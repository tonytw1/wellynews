package nz.co.searchwellington.feeds.whakaoko

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.model.{FeedItem, Subscription}
import nz.co.searchwellington.model.Feed
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class WhakaokoFeedReader @Autowired()(whakaokoService: WhakaokoService) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[WhakaokoFeedReader])

  def fetchChannelFeedItems(page: Int)(implicit ec: ExecutionContext): Future[Seq[FeedItem]] = whakaokoService.getChannelFeedItems(page)

  def fetchFeedItems(feed: Feed)(implicit ec: ExecutionContext): Future[Either[String, (Seq[FeedItem], Long)]] = {
    log.debug("Fetching feed items for feed with url: " + feed.page)

    feed.whakaokoSubscription.map { subscriptionId =>
      log.debug("Feed mapped to whakaoko subscription: " + subscriptionId)
      whakaokoService.getSubscriptionFeedItems(subscriptionId).map { result =>
        result.fold(
          { l =>
            Left(l)
          },
          { subscriptionFeedItems =>
            Right(subscriptionFeedItems)
          }
        )
      }
    }.getOrElse {
      log.warn("No whakaoko subscription found for feed: " + feed)
      Future.successful(Left("No whakaoko subscription found for feed"))
    }

  }

}
