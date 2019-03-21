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

    def lookupWhakaokoIdByUrl(url: String): Option[Int] = None // TODO implement

    feed.page.flatMap(lookupWhakaokoIdByUrl).map { id =>
      log.info("Feed url mapped to whakaoko id: " + id)
      val subscriptionFeedItems = whakaokoService.getSubscriptionFeedItems(id)
      log.info("Got " + subscriptionFeedItems.size + " feed news items from whakaoko")
      subscriptionFeedItems

    }.getOrElse {
      log.warn("Feed has no whakaoko id; skipping: " + feed.title)
      Seq()
    }
  }

}
