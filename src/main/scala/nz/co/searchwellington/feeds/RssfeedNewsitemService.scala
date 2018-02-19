package nz.co.searchwellington.feeds

import java.util.Date

import nz.co.searchwellington.feeds.reading.WhakaokoFeedReader
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem
import nz.co.searchwellington.repositories.HibernateResourceDAO
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class RssfeedNewsitemService @Autowired() (whakaokoFeedReader: WhakaokoFeedReader, var resourceDAO: HibernateResourceDAO) {

  private val log = Logger.getLogger(classOf[RssfeedNewsitemService])

  def getFeedNewsitems(feed: Feed): Seq[FrontendFeedNewsitem] = {
    whakaokoFeedReader.fetchFeedItems(feed)
  }

  final def getLatestPublicationDate(feed: Feed): Date = {
    val publicationDates = getFeedNewsitems(feed).map(i => Option(i.getDate)).flatten
    publicationDates.max  // TODO None case? By Explict about the ordering
  }

  def getFeedNewsitemByUrl(feed: Feed, url: String): Option[FrontendFeedNewsitem] = {
    getFeedNewsitems(feed).find( ni => ni.getUrl == url)
  }

  def isUrlInAcceptedFeeds(url: String): Boolean = {
    log.debug("Looking for url in accepted feeds: " + url)
    val autoAcceptFeeds = resourceDAO.getAllFeeds.filter(f => f.getAcceptancePolicy == "accept" || f.getAcceptancePolicy == "accept_without_dates")
    autoAcceptFeeds.exists { feed =>
      getFeedNewsitems(feed).exists(ni => ni.getUrl == url)
    }
  }

}
