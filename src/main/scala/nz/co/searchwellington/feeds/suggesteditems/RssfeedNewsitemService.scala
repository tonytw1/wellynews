package nz.co.searchwellington.feeds.suggesteditems

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.WhakaokoService
import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.Feed
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class RssfeedNewsitemService @Autowired()(whakaokoService: WhakaokoService) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[RssfeedNewsitemService])

  // Appears to be enhancing the getChannelFeedItems call by decorating the feed items with the feed
  def getChannelFeedItemsDecoratedWithFeeds(page: Int, feeds: Seq[Feed])(implicit ec: ExecutionContext): Future[Seq[(FeedItem, Feed)]] = {

    def decorateFeedItemsWithFeeds(feedItems: Seq[FeedItem], feeds: Seq[Feed]): Seq[(FeedItem, Feed)] = {
      feedItems.flatMap { fi =>
        val maybeFeed = feeds.find(_.whakaokoSubscription.contains(fi.subscriptionId)) // TODO use a map
        maybeFeed.map { feed =>
          (fi, feed)
        }
        //log.warn("No local feed found for feed item with subscription id (" + fi.subscriptionId + "): " + fi)
      }
    }

    for {
      channelFeedItems <- whakaokoService.getChannelFeedItems(page, Some(feeds.flatMap(_.whakaokoSubscription)))
    } yield {
      decorateFeedItemsWithFeeds(channelFeedItems, feeds)
    }
  }

}
