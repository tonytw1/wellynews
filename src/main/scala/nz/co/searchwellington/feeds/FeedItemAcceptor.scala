package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.{Newsitem, User}
import nz.co.searchwellington.utils.UrlFilters
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.stereotype.Component

@Component class FeedItemAcceptor {

  private val log = Logger.getLogger(classOf[FeedItemAcceptor])

  def acceptFeedItem(user: User, newsitem: Newsitem): Newsitem = {  // TODO input should be a feed newsitem
    log.info("Accepting: " + newsitem.title.getOrElse("") + " from feed " + newsitem.feed.map(_.toString).getOrElse(""))
    val flattenedTitle = newsitem.title.map(t => flattenLoudCapsInTitle(t))
    val date = newsitem.date2.getOrElse(DateTime.now.toDate)
    newsitem.copy(title = flattenedTitle, date2 = Some(date), accepted2 = Some(DateTime.now.toDate), acceptedBy = Some(user.id), owner = Some(user.id))
  }

  private def flattenLoudCapsInTitle(title: String) = { // TODO Push to service for testing.
    UrlFilters.lowerCappedSentence(title)
  }

}
