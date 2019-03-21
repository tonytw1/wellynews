package nz.co.searchwellington.feeds.reading

import nz.co.searchwellington.model.Feed
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Component class WhakaokoFeedReader @Autowired()(whakaokoService: WhakaokoService) {

  private val log = Logger.getLogger(classOf[WhakaokoFeedReader])

  def fetchFeedItems(): Seq[FeedItem] = {
    whakaokoService.getChannelFeedItems()
  }

  def fetchFeedItems(feed: Feed): Seq[FeedItem] = {
    log.info("Fetching feed items for feed with whakaoko id: " + feed.getWhakaokoId)

    feed.page.flatMap(whakaokoService.getWhakaokoSubscriptionByUrl).map { subscription =>
      log.info("Feed url mapped to whakaoko subscription: " + subscription.getId)
      val subscriptionFeedItems = whakaokoService.getSubscriptionFeedItems(subscription.getId)
      log.info("Got " + subscriptionFeedItems.size + " feed news items from whakaoko")
      subscriptionFeedItems

    }.getOrElse {
      log.warn("Feed has no matching whakaoko subscription; skipping: " + feed.title)
      Seq()
    }
  }

}
