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
    feed.page.flatMap(whakaokoService.getWhakaokoSubscriptionByUrl).map { subscription =>
      log.debug("Feed url mapped to whakaoko subscription: " + subscription.getId)
      val subscriptionFeedItems = whakaokoService.getSubscriptionFeedItems(subscription.getId)
      log.debug("Got " + subscriptionFeedItems.size + " feed news items from whakaoko")
      (subscriptionFeedItems, subscription)
    }
  }

}
