package nz.co.searchwellington.feeds.reading

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.reading.whakaoko.model
import nz.co.searchwellington.feeds.reading.whakaoko.model.Subscription
import nz.co.searchwellington.model.Feed
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class WhakaokoFeedReader @Autowired()(whakaokoService: WhakaokoService) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[WhakaokoFeedReader])

  def fetchChannelFeedItems(page: Int)(implicit ec: ExecutionContext): Future[Seq[model.FeedItem]] = whakaokoService.getChannelFeedItems(page)

  def fetchFeedItems(feed: Feed)(implicit ec: ExecutionContext): Future[Either[String, (Seq[model.FeedItem], Subscription)]] = {
    log.debug("Fetching feed items for feed with url: " + feed.page)

    val eventualMayBeSubscription = feed.page.map { page =>
      whakaokoService.getWhakaokoSubscriptionByUrl(page)
    }.getOrElse {
      Future.successful(None)
    }

    eventualMayBeSubscription.flatMap { mayBeSubscription =>
      mayBeSubscription.map { subscription =>
        log.debug("Feed url mapped to whakaoko subscription: " + subscription.id)

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
        Future.successful(Left("No whakaoko subscription found for feed url"))
      }
    }
  }

}
