package nz.co.searchwellington.feeds

import java.util.Date

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.reading.whakaoko.model.{FeedItem, Subscription}
import nz.co.searchwellington.feeds.reading.{WhakaokoFeedReader, WhakaokoService}
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{Await, ExecutionContext, Future}

@Component class RssfeedNewsitemService @Autowired()(whakaokoFeedReader: WhakaokoFeedReader,
                                                     mongoRepository: MongoRepository, whakaokoService: WhakaokoService)
  extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[FeedReaderRunner])

  def getChannelFeedItems()(implicit ec: ExecutionContext): Future[Seq[(FeedItem, Feed)]] = {

    def decorateFeedItemsWithFeeds(feedItmes: Seq[FeedItem], subscriptions: Seq[Subscription]): Future[Seq[(FeedItem, Feed)]] = {
      val eventualMaybeFeedsBySubscriptionId = Future.sequence(subscriptions.map { s =>
        mongoRepository.getFeedByUrl(s.url).map { fo =>
          (s.id, fo)
        }
      })

      val eventualFeedsBySubscriptionId = eventualMaybeFeedsBySubscriptionId.map { f =>
        f.flatMap { i =>
          i._2.map { f =>
            (i._1, f)
          }
        }.toMap
      }

      eventualFeedsBySubscriptionId.map { feeds =>
        feedItmes.flatMap { fi =>
          feeds.get(fi.subscriptionId).map { feed =>
            (fi, feed)
          }
        }
      }
    }

    val eventualChannelFeedItmes: Future[Seq[FeedItem]] = whakaokoFeedReader.fetchChannelFeedItems
    val eventualSubscriptions: Future[Seq[Subscription]] = whakaokoService.getSubscriptions()

    for {
      channelFeedItems <- eventualChannelFeedItmes
      subscriptions <- eventualSubscriptions
      channelFeedItemsWithFeeds <- decorateFeedItemsWithFeeds(channelFeedItems, subscriptions)

    } yield {
      channelFeedItemsWithFeeds
    }

  }

  def getFeedItemsAndDetailsFor(feed: Feed)(implicit ec: ExecutionContext): Future[Either[String, (Seq[FeedItem], Subscription)]] = {
    log.info("Getting feed items for: " + feed.title + " / " + feed.page)
    whakaokoFeedReader.fetchFeedItems(feed)
  }

  def getLatestPublicationDate(feed: Feed)(implicit ec: ExecutionContext): Future[Option[Date]] = {
    getFeedItemsAndDetailsFor(feed).map { r =>
      r.toOption.flatMap { right =>
        latestPublicationDateOf(right._1)
      }
    }
  }

  def latestPublicationDateOf(feedItems: Seq[FeedItem]): Option[Date] = {
    val publicationDates = feedItems.flatMap(fi => fi.date.map(_.toDate))
    if (publicationDates.nonEmpty) {
      Some(publicationDates.max)
    } else {
      None
    }
  }

  def getFeedNewsitemByUrl(feed: Feed, url: String): Option[(FeedItem)] = {
    getFeedItemsFor(feed).flatMap { fis =>
      fis.find(ni => ni.url == url)
    }
  }

  def isUrlInAcceptedFeeds(url: String): Boolean = { // TODO should be option
    val autoAcceptFeeds = Await.result(mongoRepository.getAllFeeds, TenSeconds).filter { f =>
      f.acceptance == FeedAcceptancePolicy.ACCEPT || f.getAcceptancePolicy == FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES
    }
    autoAcceptFeeds.exists { feed =>
      getFeedItemsFor(feed).getOrElse(Seq.empty).exists(ni => ni.url == url)
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
