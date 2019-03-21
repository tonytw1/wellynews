package nz.co.searchwellington.feeds

import java.util.Calendar

import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.AutoTaggingService
import nz.co.searchwellington.utils.UrlCleaner
import org.apache.log4j.Logger
import org.joda.time.{DateTime, DateTimeZone}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.bson.BSONObjectID
import uk.co.eelpieconsulting.common.dates.DateFormatter
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Component class FeedReader @Autowired()(mongoRepository: MongoRepository, rssfeedNewsitemService: RssfeedNewsitemService,
                                         feedAcceptanceDecider: FeedAcceptanceDecider, urlCleaner: UrlCleaner,
                                         contentUpdateService: ContentUpdateService, autoTagger: AutoTaggingService,
                                         linkCheckerQueue: LinkCheckerQueue, feedReaderUpdateService: FeedReaderUpdateService) {

  private val log = Logger.getLogger(classOf[FeedReader])

  private val dateFormatter = new DateFormatter(DateTimeZone.UTC) // TODO inject

  def processFeed(feedId: BSONObjectID, loggedInUser: User, manuallySpecifiedAcceptancePolicy: FeedAcceptancePolicy): Unit = { // TODO interface should be feeds not feed ids?
    val feed = mongoRepository.getResourceByObjectId(feedId).asInstanceOf[Feed]
    processFeed(feed, loggedInUser, manuallySpecifiedAcceptancePolicy)
  }

  def processFeed(feed: Feed, loggedInUser: User): Unit = {
    processFeed(feed, loggedInUser, feed.getAcceptancePolicy)
  }

  private def processFeed(feed: Feed, feedReaderUser: User, acceptancePolicy: FeedAcceptancePolicy) = {

    def markFeedAsRead(feed: Feed): Unit = {
      feed.setLatestItemDate(rssfeedNewsitemService.getLatestPublicationDate(feed))
      log.info("Feed latest item publication date is: " + feed.getLatestItemDate)
      feed.setLastRead(Calendar.getInstance.getTime)
      // TODO contentUpdateService.update(feed)
    }

    try {
      log.info("Processing feed: " + feed.title + " using acceptance policy '" + acceptancePolicy + "'. Last read: " + feed.lastRead.map(dateFormatter.timeSince))
      val feedNewsitems = rssfeedNewsitemService.getFeedItemsFor(feed)
      log.info("Feed contains " + feedNewsitems.size + " items")
      feed.setHttpStatus(if (feedNewsitems.nonEmpty) 200 else -3)
      if (acceptancePolicy.shouldReadFeed) processFeedItems(feed, feedReaderUser, acceptancePolicy, feedNewsitems.map(i => i._1))
      markFeedAsRead(feed)
      log.info("Done processing feed.")

    } catch {
      case e: Exception =>
        log.error(e, e)
    }
  }

  private def processFeedItems(feed: Feed, feedReaderUser: User, acceptancePolicy: FeedAcceptancePolicy, feedNewsitems: Seq[FeedItem]) = {
    log.info("Accepting feed items")

    feedNewsitems.map { feednewsitem => // TODO new up a new copy before modifying

      val cleanSubmittedItemUrl = urlCleaner.cleanSubmittedItemUrl(feednewsitem.getUrl)
      feednewsitem.setUrl(cleanSubmittedItemUrl)    // TODO do not mutate inputs

      val acceptanceErrors = feedAcceptanceDecider.getAcceptanceErrors(feed, feednewsitem, acceptancePolicy)
      if (acceptanceErrors.isEmpty) {
        log.info("Accepting newsitem: " + feednewsitem.getUrl)
        val acceptedNewsitem = feedReaderUpdateService.acceptNewsitem(feedReaderUser, feednewsitem, feed)
        linkCheckerQueue.add(acceptedNewsitem)

      } else {
        log.info("Not accepting " + feednewsitem.getUrl + " due to acceptance errors: " + acceptanceErrors)
      }
    }
  }

}
