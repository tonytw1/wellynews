package nz.co.searchwellington.feeds

import nz.co.searchwellington.controllers.submission.EndUserInputs
import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, Newsitem, User}
import nz.co.searchwellington.urls.UrlCleaner
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class FeedItemAcceptor @Autowired()(feeditemToNewsItemSerice: FeeditemToNewsitemService,
                                               val urlCleaner: UrlCleaner) extends EndUserInputs {

  def acceptFeedItem(feedReadingUser: User, feeditem: (FeedItem, Feed)): Newsitem = {
    val newsitem = feeditemToNewsItemSerice.makeNewsitemFromFeedItem(feeditem._1, feeditem._2)

    val now = DateTime.now.toDate
    val dateToApply = feeditem._2.acceptance match {
      case FeedAcceptancePolicy.ACCEPT_IGNORING_DATE => now
      case _ => newsitem.date.getOrElse(now)
    }
    newsitem.copy(
      title = newsitem.title.map(processTitle),
      page = cleanUrl(newsitem.page),
      date = Some(dateToApply),
      accepted = Some(now),
      acceptedBy = Some(feedReadingUser._id),
      owner = Some(feedReadingUser._id)
    )
  }

}
