package nz.co.searchwellington.feeds

import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, Newsitem, User}
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class FeedItemAcceptor @Autowired()(feeditemToNewsItemService: FeeditemToNewsitemService) {

  def acceptFeedItem(acceptingUser: User, feedItemAndFeed: (FeedItem, Feed)): Option[Newsitem] = {
    // Given a feed item attempt to transform it into an acceptable news item
    val feedItem = feedItemAndFeed._1
    val feed = feedItemAndFeed._2

    feeditemToNewsItemService.makeNewsitemFromFeedItem(feedItem, feed).map { newsitem =>
      // Apply acceptance details to the news item
      val now = DateTime.now.toDate
      val dateToApply = feed.acceptance match {
        case FeedAcceptancePolicy.ACCEPT_IGNORING_DATE => now
        case _ => newsitem.date.getOrElse(feedItem.accepted.map(_.toDate).getOrElse(now))
      }
      newsitem.copy(
        date = Some(dateToApply),
        accepted = Some(now),
        acceptedBy = Some(acceptingUser._id),
        owner = Some(acceptingUser._id)
      )
    }
  }

}
