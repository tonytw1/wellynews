package nz.co.searchwellington.feeds.whakaoko

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.model.{FeedItem, Subscription}
import nz.co.searchwellington.model.Feed
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class WhakaokoFeedReader @Autowired()(whakaokoService: WhakaokoService) extends ReasonableWaits {

  /*
    Whakaoko (https://github.com/tonytw1/whakaoko) is an RSS reading and aggregation service.
    It allows us to subscribe to an RSS feed URL and get the feed items back in JSON format.
    Multiple RSS feed subscriptions can be aggregated into a channel.
    This package handles the interactions with Whakaoko.
   */

  private val log = Logger.getLogger(classOf[WhakaokoFeedReader])

  def fetchChannelFeedItems(page: Int)(implicit ec: ExecutionContext): Future[Seq[FeedItem]] = whakaokoService.getChannelFeedItems(page)

  def fetchFeedItems(feed: Feed)(implicit ec: ExecutionContext): Future[Either[String, ((Seq[FeedItem], Long), Subscription)]] = {
    log.debug("Fetching feed items for feed with url: " + feed.page)
    whakaokoService.getWhakaokoSubscriptionFor(feed).flatMap { mayBeSubscription =>
      mayBeSubscription.map { subscription =>
        log.debug("Feed mapped to whakaoko subscription: " + subscription.id)
        whakaokoService.getSubscriptionFeedItems(subscription.id).map { result =>
          result.fold(
            { l =>
              Left(l)
            },
            { subscriptionFeedItems =>
              Right((subscriptionFeedItems, subscription))
            }
          )
        }

      }.getOrElse {
        log.warn("No whakaoko subscription found for feed url: " + feed)
        Future.successful(Left("No whakaoko subscription found for feed url"))
      }
    }
  }

}
