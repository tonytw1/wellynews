package nz.co.searchwellington.feeds.reading

import nz.co.searchwellington.model.Feed
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.{FeedItem, Subscription}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Component class WhakaokoFeedReader @Autowired()(whakaokoService: WhakaokoService) {

  private val log = Logger.getLogger(classOf[WhakaokoFeedReader])

  def fetchChannelFeedItems(): Future[Seq[FeedItem]] = {
    whakaokoService.getChannelFeedItems()
  }

  def fetchFeedItems(feed: Feed): Future[Either[String, (Seq[FeedItem], Subscription)]] = {
    log.debug("Fetching feed items for feed with url: " + feed.page)
    feed.page.flatMap(whakaokoService.getWhakaokoSubscriptionByUrl).map { subscription =>
      log.debug("Feed url mapped to whakaoko subscription: " + subscription.getId)

      val eventualFutureFeedItems = whakaokoService.getSubscriptionFeedItems(subscription.getId)
      eventualFutureFeedItems.map { result =>
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
