package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, Newsitem, User}
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class FeedItemAcceptor @Autowired()() {

  def acceptFeedItem(feedReadingUser: User, newsitemAndFeed: (Newsitem, Feed)): Newsitem = {
    val newsitem = newsitemAndFeed._1
    val now = DateTime.now.toDate
    val dateToApply = newsitemAndFeed._2.acceptance match {
      case FeedAcceptancePolicy.ACCEPT_IGNORING_DATE => now
      case _ => newsitem.date.getOrElse(now)
    }
    newsitem.copy(
      date = Some(dateToApply),
      accepted = Some(now),
      acceptedBy = Some(feedReadingUser._id),
      owner = Some(feedReadingUser._id)
    )
  }

}
