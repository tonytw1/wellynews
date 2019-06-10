package nz.co.searchwellington.feeds

import nz.co.searchwellington.feeds.reading.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, Newsitem, User}
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class FeedItemAcceptor @Autowired() (feeditemToNewsItemSerice: FeeditemToNewsitemService) {

  def acceptFeedItem(feedReadingUser: User, feeditem: (FeedItem, Feed)): Newsitem = {
    val newsitem = feeditemToNewsItemSerice.makeNewsitemFromFeedItem(feeditem._1, feeditem._2)
    newsitem.copy(
      accepted = Some(DateTime.now.toDate),
      acceptedBy = Some(feedReadingUser._id),
      owner = Some(feedReadingUser._id)
    )
  }

}
