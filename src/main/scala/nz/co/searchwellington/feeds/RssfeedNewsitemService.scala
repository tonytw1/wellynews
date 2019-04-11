package nz.co.searchwellington.feeds

import java.util.Date

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.reading.WhakaokoFeedReader
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

import scala.concurrent.Await

@Component class RssfeedNewsitemService @Autowired()(whakaokoFeedReader: WhakaokoFeedReader, mongoRepository: MongoRepository)
  extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[FeedReaderRunner])

  def getChannelFeedItems(): Seq[(FeedItem, Feed)] = {
    whakaokoFeedReader.fetchChannelFeedItems().flatMap { i =>
      Await.result(mongoRepository.getFeedByWhakaokoSubscription(i.getSubscriptionId), TenSeconds).map { feed =>
        (i, feed)
      } // TODO log about missing feeds
    }
  }

  def getFeedItemsFor(feed: Feed): Option[Seq[FeedItem]] = {
    log.info("Getting feed items for: " + feed.title + " / " + feed.page)
    whakaokoFeedReader.fetchFeedItems(feed).fold(
      { l =>
        log.warn("Fetch feed items failed for " + feed.title + ": " + l)
        None
      }, { r =>
        Some(r._1)
      }
    )
  }

  def getLatestPublicationDate(feed: Feed): Option[Date] = {
    getFeedItemsFor(feed).flatMap { fis =>
      val publicationDates = fis.flatMap(fi => Option(fi.getDate))
      if (publicationDates.nonEmpty) {
        Some(publicationDates.max)
      } else {
        None
      }
    }
  }

  def getFeedNewsitemByUrl(feed: Feed, url: String): Option[(FeedItem)] = {
    getFeedItemsFor(feed).flatMap { fis =>
      fis.find(ni => ni.getUrl == url)
    }
  }

  def isUrlInAcceptedFeeds(url: String): Boolean = { // TODO should be option
    val autoAcceptFeeds = Await.result(mongoRepository.getAllFeeds, TenSeconds).filter { f =>
      f.acceptance == FeedAcceptancePolicy.ACCEPT || f.getAcceptancePolicy == FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES
    }
    autoAcceptFeeds.exists { feed =>
      getFeedItemsFor(feed).getOrElse(Seq.empty).exists(ni => ni.getUrl == url)
    }
  }

}
