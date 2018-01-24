package nz.co.searchwellington.feeds

import java.util.Calendar

import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, User}
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.repositories.HibernateResourceDAO
import nz.co.searchwellington.tagging.AutoTaggingService
import nz.co.searchwellington.utils.UrlCleaner
import org.apache.log4j.Logger
import org.joda.time.DateTimeZone
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.{Propagation, Transactional}
import uk.co.eelpieconsulting.common.dates.DateFormatter

@Component class FeedReader @Autowired() (resourceDAO: HibernateResourceDAO, rssfeedNewsitemService: RssfeedNewsitemService, feedAcceptanceDecider: FeedAcceptanceDecider, urlCleaner: UrlCleaner, contentUpdateService: ContentUpdateService, autoTagger: AutoTaggingService,
                                         linkCheckerQueue: LinkCheckerQueue, feedReaderUpdateService: FeedReaderUpdateService) {

  private val log = Logger.getLogger(classOf[FeedReader])

  private val dateFormatter = new DateFormatter(DateTimeZone.UTC) // TODO inject

  @Transactional(propagation = Propagation.REQUIRES_NEW) def processFeed(feedId: Int, loggedInUser: User, manuallySpecifiedAcceptancePolicy: FeedAcceptancePolicy): Unit = { // TODO interface should be feeds not feed ids?
    val feed = resourceDAO.loadResourceById(feedId).asInstanceOf[Feed]
    processFeed(feed, loggedInUser, manuallySpecifiedAcceptancePolicy)
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW) def processFeed(feedId: Int, loggedInUser: User): Unit = {
    val feed = resourceDAO.loadResourceById(feedId).asInstanceOf[Feed]
    processFeed(feed, loggedInUser, feed.getAcceptancePolicy)
  }

  private def processFeed(feed: Feed, feedReaderUser: User, acceptancePolicy: FeedAcceptancePolicy) = {
    try {
      log.info("Processing feed: " + feed.getName + " using acceptance policy '" + acceptancePolicy + "'. Last read: " + dateFormatter.timeSince(feed.getLastRead))
      val feedNewsitems = rssfeedNewsitemService.getFeedNewsitems(feed)
      log.info("Feed contains " + feedNewsitems.size + " items")
      feed.setHttpStatus(if (!feedNewsitems.isEmpty) 200 else -3)
      if (acceptancePolicy.shouldReadFeed) processFeedItems(feed, feedReaderUser, acceptancePolicy, feedNewsitems)
      markFeedAsRead(feed)
      log.info("Done processing feed.")
    } catch {
      case e: Exception =>
        log.error(e, e)
    }
  }

  private def processFeedItems(feed: Feed, feedReaderUser: User, acceptancePolicy: FeedAcceptancePolicy, feedNewsitems: Seq[FrontendFeedNewsitem]) = {
    log.info("Accepting feed items")
    for (feednewsitem <- feedNewsitems) { // TODO new up a new copy before modifying
      val cleanSubmittedItemUrl = urlCleaner.cleanSubmittedItemUrl(feednewsitem.getUrl)
      feednewsitem.setUrl(cleanSubmittedItemUrl)
      val acceptanceErrors = feedAcceptanceDecider.getAcceptanceErrors(feed, feednewsitem, acceptancePolicy)
      val acceptThisItem = acceptanceErrors.isEmpty
      if (acceptThisItem) {
        log.info("Accepting newsitem: " + feednewsitem.getUrl)
        linkCheckerQueue.add(feedReaderUpdateService.acceptNewsitem(feed, feedReaderUser, feednewsitem))
      } else {
        log.info("Not accepting " + feednewsitem.getUrl + " due to acceptance errors: " + acceptanceErrors)
      }
    }
  }

  def markFeedAsRead(feed: Feed): Unit = {
    feed.setLatestItemDate(rssfeedNewsitemService.getLatestPublicationDate(feed))
    log.info("Feed latest item publication date is: " + feed.getLatestItemDate)
    feed.setLastRead(Calendar.getInstance.getTime)
    contentUpdateService.update(feed)
  }

}
