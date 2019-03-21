package nz.co.searchwellington.feeds

import java.util.Date

import nz.co.searchwellington.feeds.reading.WhakaokoFeedReader
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, MINUTES, SECONDS}

@Component class RssfeedNewsitemService @Autowired() (whakaokoFeedReader: WhakaokoFeedReader, mongoRepository: MongoRepository) {

  private val log = Logger.getLogger(classOf[FeedReaderRunner])
  private val tenSeconds = Duration(10, SECONDS)

  def getFeedItems(): Seq[(FeedItem, Option[Feed])] = {
    whakaokoFeedReader.fetchFeedItems().map { i =>
      val feed = Await.result(mongoRepository.getFeedByWhakaokoSubscription(i.getSubscriptionId), Duration(1, MINUTES))
      (i, feed)
    }
  }

  def getFeedItemsFor(feed: Feed): Option[(Seq[FeedItem], Feed)] = {
    log.info("Getting feed items for: " + feed.title + " / " + feed.page)
    whakaokoFeedReader.fetchFeedItems(feed).map(i => (i, feed))
  }

  def getLatestPublicationDate(feed: Feed): Option[Date] = {
    getFeedItemsFor(feed).flatMap { fis =>
      val publicationDates = fis._1.flatMap(fi => Option(fi.getDate))
      if (publicationDates.nonEmpty) {
        Some(publicationDates.max)
      } else {
        None
      }
    }
  }

  def getFeedNewsitemByUrl(feed: Feed, url: String): Option[(FeedItem, Feed)] = {
    getFeedItemsFor(feed).flatMap { fis =>
      val a: (Seq[FeedItem], Feed) = fis
      fis._1.find(ni => ni.getUrl == url).map((_, feed))
    }
  }

  def isUrlInAcceptedFeeds(url: String): Boolean = {
    val autoAcceptFeeds: Seq[Feed] = Await.result(mongoRepository.getAllFeeds, tenSeconds).
      filter(f => f.getAcceptancePolicy == "accept" || f.getAcceptancePolicy == "accept_without_dates")
    autoAcceptFeeds.exists { feed =>
      getFeedItemsFor(feed).map(i => i._1).getOrElse(Seq.empty).exists(ni => ni.getUrl == url)
    }
  }

}
