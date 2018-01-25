package nz.co.searchwellington.feeds.reading

import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class WhakaokoFeedReader @Autowired()(whakaoroService: WhakaokoService, whakaokoFeedItemMapper: WhakaokoFeedItemMapper) {

  private val log = Logger.getLogger(classOf[WhakaokoFeedReader])

  def fetchFeedItems(feed: Feed): Seq[FrontendFeedNewsitem] = {
    log.info("Fetching feed items for feed with whakaoko id: " + feed.getWhakaokoId)
    Option(feed.getWhakaokoId).map { whakaokoId =>
      val subscriptionFeedItems = whakaoroService.getSubscriptionFeedItems(whakaokoId)
      val results = subscriptionFeedItems.map { feedItem =>
        whakaokoFeedItemMapper.mapWhakaokoFeeditem(feed, feedItem)
      }
      log.info("Got " + results.size + " feed news items from whakaoko")
      results

    }.getOrElse {
      log.warn("Feed has no whakaoro id; skipping: " + feed.getName)
      Seq()
    }
  }
}