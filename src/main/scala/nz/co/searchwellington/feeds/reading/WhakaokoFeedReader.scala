package nz.co.searchwellington.feeds.reading

import nz.co.searchwellington.model.Feed
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.{FeedItem, Subscription}

@Component class WhakaokoFeedReader @Autowired()(whakaokoService: WhakaokoService) {

  private val log = Logger.getLogger(classOf[WhakaokoFeedReader])

  def fetchChannelFeedItems(): Seq[FeedItem] = {
    whakaokoService.getChannelFeedItems()
  }

  def fetchFeedItems(feed: Feed): Either[String, (Seq[FeedItem], Subscription)] = {
    log.debug("Fetching feed items for feed with url: " + feed.page)
    feed.page.flatMap(whakaokoService.getWhakaokoSubscriptionByUrl).map { subscription =>
      log.debug("Feed url mapped to whakaoko subscription: " + subscription.getId)

      whakaokoService.getSubscriptionFeedItems(subscription.getId).fold(
        { l =>
          Left(l)
        },
        { subscriptionFeedItems =>
          Right((subscriptionFeedItems, subscription))
        }
      )

    }.getOrElse {
      Left("No whakaoko subscription found for feed url")
    }
  }

}
