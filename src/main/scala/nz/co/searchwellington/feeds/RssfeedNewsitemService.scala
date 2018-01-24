package nz.co.searchwellington.feeds

import java.util.Date
import java.util
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem
import nz.co.searchwellington.repositories.HibernateResourceDAO
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class RssfeedNewsitemService @Autowired()(cachingRssfeedNewsitemService: CachingRssfeedNewsitemService, var resourceDAO: HibernateResourceDAO) {

  private val log = Logger.getLogger(classOf[RssfeedNewsitemService])

  def getFeedNewsitems(feed: Feed): util.List[FrontendFeedNewsitem] = {
    return cachingRssfeedNewsitemService.getFeedNewsitems(feed)
  }

  final def getLatestPublicationDate(feed: Feed): Date = {
    var latestPublicationDate: Date = null
    val feeditems: util.List[FrontendFeedNewsitem] = getFeedNewsitems(feed)

    import scala.collection.JavaConversions._

    for (feeditem <- feeditems) {
      if (feeditem.getDate != null && (latestPublicationDate == null || feeditem.getDate.after(latestPublicationDate))) {
        latestPublicationDate = feeditem.getDate
      }
    }
    return latestPublicationDate
  }

  def getFeedNewsitemByUrl(feed: Feed, url: String): FrontendFeedNewsitem = {
    val feedNewsitems: util.List[FrontendFeedNewsitem] = this.getFeedNewsitems(feed)
    val i: util.Iterator[FrontendFeedNewsitem] = feedNewsitems.iterator
    while ( {
      i.hasNext
    }) {
      val feedNewsitem: FrontendFeedNewsitem = i.next
      if (feedNewsitem.getUrl != null && feedNewsitem.getUrl == url) {
        return feedNewsitem
      }
    }
    return null
  }

  def isUrlInAcceptedFeeds(url: String): Boolean = {
    log.debug("Looking for url in accepted feeds: " + url)

    import scala.collection.JavaConversions._

    for (feed <- resourceDAO.getAllFeeds) {
      if (feed.getAcceptancePolicy == "accept" || feed.getAcceptancePolicy == "accept_without_dates") {
        log.debug("Checking feed: " + feed.getName)
        val feednewsItems: util.List[FrontendFeedNewsitem] = this.getFeedNewsitems(feed)

        import scala.collection.JavaConversions._

        for (feedNewsitem <- feednewsItems) {
          log.debug("Checking feeditem: " + feedNewsitem.getUrl)
          if (feedNewsitem.getUrl == url) {
            log.debug("Found: " + url)
            return true
          }
        }
      }
    }
    return false
  }

}
