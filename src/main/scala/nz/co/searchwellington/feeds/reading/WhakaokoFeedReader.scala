package nz.co.searchwellington.feeds.reading

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.reading.whakaoko.model
import nz.co.searchwellington.feeds.reading.whakaoko.model.Subscription
import nz.co.searchwellington.model.Feed
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Component class WhakaokoFeedReader @Autowired()(whakaokoService: WhakaokoService) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[WhakaokoFeedReader])

  def fetchChannelFeedItems(): Future[Seq[model.FeedItem]] = {
    whakaokoService.getChannelFeedItems()
  }

  def fetchFeedItems(feed: Feed): Future[Either[String, (Seq[model.FeedItem], Subscription)]] = {
    log.debug("Fetching feed items for feed with url: " + feed.page)

    val mayBeSubscription = feed.page.flatMap { page =>
      Await.result(whakaokoService.getWhakaokoSubscriptionByUrl(page), TenSeconds)
    }

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
