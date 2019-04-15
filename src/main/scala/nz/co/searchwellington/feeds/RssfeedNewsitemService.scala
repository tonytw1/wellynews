package nz.co.searchwellington.feeds

import java.util.Date

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.reading.WhakaokoFeedReader
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.{FeedItem, Subscription}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Component class RssfeedNewsitemService @Autowired()(whakaokoFeedReader: WhakaokoFeedReader, mongoRepository: MongoRepository)
  extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[FeedReaderRunner])

  def getChannelFeedItems: Future[Seq[(FeedItem, Feed)]] = {
    whakaokoFeedReader.fetchChannelFeedItems().flatMap { channelFeedItems =>
      val eventualMappedFeeds = channelFeedItems.map { i =>
        mongoRepository.getFeedByWhakaokoSubscription(i.getSubscriptionId).map { maybeFeed =>
          maybeFeed.map { feed =>
            (i, feed)
          } // TODO log missing feeds
        }
      }
      Future.sequence(eventualMappedFeeds).map(_.flatten)
    }
  }

  def getFeedItemsAndDetailsFor(feed: Feed): Future[Either[String, (Seq[FeedItem], Subscription)]] = {
    log.info("Getting feed items for: " + feed.title + " / " + feed.page)
    whakaokoFeedReader.fetchFeedItems(feed)
  }

  def getLatestPublicationDate(feed: Feed): Future[Option[Date]] = {
    getFeedItemsAndDetailsFor(feed).map { r =>
      r.toOption.flatMap { right =>
        val publicationDates = right._1.flatMap(fi => Option(fi.getDate))
        if (publicationDates.nonEmpty) {
          Some(publicationDates.max)
        } else {
          None
        }
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

  @Deprecated() // Should really use the Either return
  private def getFeedItemsFor(feed: Feed): Option[Seq[FeedItem]] = {
    log.info("Getting feed items for: " + feed.title + " / " + feed.page)
    val feedItems = Await.result(whakaokoFeedReader.fetchFeedItems(feed), TenSeconds)
    feedItems.fold(
      { l =>
        log.warn("Fetch feed items failed for " + feed.title + ": " + l)
        None
      }, { r =>
        Some(r._1)
      }
    )
  }

}
