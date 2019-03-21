package nz.co.searchwellington.feeds

import java.util.Date

import nz.co.searchwellington.feeds.reading.WhakaokoFeedReader
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.repositories.HibernateResourceDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, MINUTES}

@Component class RssfeedNewsitemService @Autowired() (whakaokoFeedReader: WhakaokoFeedReader, mongoRepository: MongoRepository, resourceDAO: HibernateResourceDAO) {

  private val log = Logger.getLogger(classOf[FeedReaderRunner])

  def getFeedItems(): Seq[(FeedItem, Option[Feed])] = {
    whakaokoFeedReader.fetchFeedItems().map { i =>
      val feed = Await.result(mongoRepository.getFeedByWhakaokoSubscription(i.getSubscriptionId), Duration(1, MINUTES))
      (i, feed)
    }
  }
  def getFeedItemsFor(feed: Feed): Seq[(FeedItem, Option[Feed])] = {
    log.info("Getting feed items for: " + feed.title + " / " + feed.page)
    whakaokoFeedReader.fetchFeedItems(feed).map(i => (i, Some(feed)))
  }

  final def getLatestPublicationDate(feed: Feed): Date = {
    val publicationDates = getFeedItemsFor(feed).map(i => Option(i._1.getDate)).flatten
    publicationDates.max  // TODO None case? By Explict about the ordering
  }

  def getFeedNewsitemByUrl(feed: Feed, url: String): Option[(FeedItem, Option[Feed])] = {
    getFeedItemsFor(feed).find(ni => ni._1.getUrl == url)
  }

  def isUrlInAcceptedFeeds(url: String): Boolean = {
    val autoAcceptFeeds = resourceDAO.getAllFeeds.filter(f => f.getAcceptancePolicy == "accept" || f.getAcceptancePolicy == "accept_without_dates")
    autoAcceptFeeds.exists { feed =>
      getFeedItemsFor(feed).exists(ni => ni._1.getUrl == url)
    }
  }

}
