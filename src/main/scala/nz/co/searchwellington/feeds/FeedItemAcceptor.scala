package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.{Feed, Newsitem, User}
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Component class FeedItemAcceptor @Autowired() (feeditemToNewsItemSerice: FeeditemToNewsitemService) {

  private val log = Logger.getLogger(classOf[FeedItemAcceptor])

  def acceptFeedItem(feedReadingUser: User, feeditem: (FeedItem, Feed)): Newsitem = {
    val newsitem = feeditemToNewsItemSerice.makeNewsitemFromFeedItem(feeditem._1, feeditem._2)
    newsitem.copy(
      accepted = Some(DateTime.now.toDate),
      acceptedBy = Some(feedReadingUser._id),
      owner = Some(feedReadingUser._id)
    )
  }

}
