package nz.co.searchwellington.feeds.reading

import nz.co.searchwellington.model.Feed
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.{FeedItem, Subscription}

@Component class WhakaokoFeedReader @Autowired()(whakaokoService: WhakaokoService) {

  private val log = Logger.getLogger(classOf[WhakaokoFeedReader])

  def fetchFeedItems(): Seq[FeedItem] = {
    whakaokoService.getChannelFeedItems()
  }

  def fetchFeedItems(feed: Feed): Option[(Seq[FeedItem], Subscription)] = {
    log.debug("Fetching feed items for feed with url: " + feed.page)
    feed.page.flatMap(whakaokoService.getWhakaokoSubscriptionByUrl).flatMap { subscription =>
      log.debug("Feed url mapped to whakaoko subscription: " + subscription.getId)

      whakaokoService.getSubscriptionFeedItems(subscription.getId).fold(
        { l =>
          log.warn("Feed fetch failed with: " + l)
          None
        },
        { result =>
          val subscriptionFeedItems: Seq[FeedItem] = result
          log.debug("Got " + subscriptionFeedItems.size + " feed news items from whakaoko")
          Some((subscriptionFeedItems, subscription))
        }
      )
    }
  }

}
