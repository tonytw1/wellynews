package nz.co.searchwellington.feeds.reading

import nz.co.searchwellington.model.Feed
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Component class WhakaokoFeedReader @Autowired()(whakaokoService: WhakaokoService, whakaokoFeedItemMapper: WhakaokoFeedItemMapper) {

  private val log = Logger.getLogger(classOf[WhakaokoFeedReader])

  def fetchFeedItems(feed: Feed): Seq[FeedItem] = {
    log.info("Fetching feed items for feed with whakaoko id: " + feed.getWhakaokoId)
    Option(feed.getWhakaokoId).map { whakaokoId =>
      val subscriptionFeedItems: Seq[FeedItem] = whakaokoService.getSubscriptionFeedItems(whakaokoId)
      //val results = subscriptionFeedItems.map { feedItem =>
      //  whakaokoFeedItemMapper.mapWhakaokoFeeditem(feed, feedItem)
      //}
      log.info("Got " + subscriptionFeedItems.size + " feed news items from whakaoko")
      subscriptionFeedItems

    }.getOrElse {
      log.warn("Feed has no whakaoko id; skipping: " + feed.title)
      Seq()
    }
  }
}
