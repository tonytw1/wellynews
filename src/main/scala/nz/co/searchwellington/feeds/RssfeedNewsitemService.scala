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

  def getChannelFeedItems(page: Int)(implicit ec: ExecutionContext): Future[Seq[(FeedItem, Feed)]] = {

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

    val eventualSubscriptions = whakaokoService.getSubscriptions
    val eventualChannelFeedItems = whakaokoFeedReader.fetchChannelFeedItems(page)

    for {
      channelFeedItems <- eventualChannelFeedItems
      subscriptions <- eventualSubscriptions
      channelFeedItemsWithFeeds <- decorateFeedItemsWithFeeds(channelFeedItems, subscriptions)

    } yield {
      channelFeedItemsWithFeeds
    }

  }

  def getFeedItemsAndDetailsFor(feed: Feed)(implicit ec: ExecutionContext): Future[Either[String, (Seq[FeedItem], Subscription)]] = {
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

  // TODO this is interesting but always suppressing when deleting a newsitem which same from a feed would be simpler
  def isUrlInAcceptedFeeds(url: String)(implicit ec: ExecutionContext): Future[Boolean] = { // TODO should be option
    @Deprecated() // Should really use the Either return
    def getFeedItemsFor(feed: Feed)(implicit ec: ExecutionContext): Future[Option[Seq[FeedItem]]] = {
      whakaokoFeedReader.fetchFeedItems(feed).map { feedItems =>
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

    val eventualAutoAcceptingFeeds = mongoRepository.getAllFeeds.map { autoAcceptFeeds =>
      autoAcceptFeeds.filter { f =>
        f.acceptance == FeedAcceptancePolicy.ACCEPT || f.getAcceptancePolicy == FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES
      }
    }
    eventualAutoAcceptingFeeds.map { autoAcceptFeeds =>
      autoAcceptFeeds.exists { feed =>
        Await.result(getFeedItemsFor(feed), TenSeconds).getOrElse(Seq.empty).exists(ni => ni.url == url)
      }
    }
  }


}
