package nz.co.searchwellington.feeds

import java.util.Date

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.WhakaokoFeedReader
import nz.co.searchwellington.feeds.whakaoko.model.{FeedItem, Subscription}
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class RssfeedNewsitemService @Autowired()(whakaokoFeedReader: WhakaokoFeedReader,
                                                     mongoRepository: MongoRepository)
  extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[FeedReaderRunner])

  def getChannelFeedItems(page: Int, subscriptions: Seq[Subscription])(implicit ec: ExecutionContext): Future[Seq[(FeedItem, Feed)]] = {

    def decorateFeedItemsWithFeeds(feedItems: Seq[FeedItem], subscriptions: Seq[Subscription]): Future[Seq[(FeedItem, Feed)]] = {
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
        feedItems.flatMap { fi =>

          if (feeds.get(fi.subscriptionId).isEmpty) {
            log.warn("No local feed found for feed item with subscription id (" + fi.subscriptionId + "): " + fi)
          }

          feeds.get(fi.subscriptionId).map { feed =>
            (fi, feed)
          }
        }
      }
    }

    for {
      channelFeedItems <- whakaokoFeedReader.fetchChannelFeedItems(page)
      channelFeedItemsWithFeeds <- decorateFeedItemsWithFeeds(channelFeedItems, subscriptions)

    } yield {
      channelFeedItemsWithFeeds
    }

  }

  def getFeedItemsAndDetailsFor(feed: Feed)(implicit ec: ExecutionContext): Future[Either[String, ((Seq[FeedItem], Long), Subscription)]] = {
    whakaokoFeedReader.fetchFeedItems(feed)
  }

  def getLatestPublicationDate(feed: Feed)(implicit ec: ExecutionContext): Future[Option[Date]] = {
    getFeedItemsAndDetailsFor(feed).map { r =>
      r.toOption.flatMap { right =>
        latestPublicationDateOf(right._1._1)
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

}
