package nz.co.searchwellington.feeds

import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, Newsitem, User}
import nz.co.searchwellington.utils.StringWrangling
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class FeedItemAcceptor @Autowired()(feeditemToNewsItemSerice: FeeditemToNewsitemService) extends StringWrangling {

  def acceptFeedItem(feedReadingUser: User, feeditem: (FeedItem, Feed)): Newsitem = {
    val newsitem = feeditemToNewsItemSerice.makeNewsitemFromFeedItem(feeditem._1, feeditem._2)

    val accepted = Some(DateTime.now.toDate)
    val dateToApply = feeditem._2.acceptance match {
      case FeedAcceptancePolicy.ACCEPT_IGNORING_DATE => accepted
      case _ => newsitem.date
    }
    newsitem.copy(
      title = newsitem.title.map(lowerCappedSentence),
      date = dateToApply,
      accepted = accepted,
      acceptedBy = Some(feedReadingUser._id),
      owner = Some(feedReadingUser._id)
    )
  }

}
