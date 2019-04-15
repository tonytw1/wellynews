package nz.co.searchwellington.feeds

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.tagging.AutoTaggingService
import nz.co.searchwellington.utils.UrlCleaner
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Component class FeedReader @Autowired()(rssfeedNewsitemService: RssfeedNewsitemService,
                                         feedAcceptanceDecider: FeedAcceptanceDecider, urlCleaner: UrlCleaner,
                                         contentUpdateService: ContentUpdateService, autoTagger: AutoTaggingService,
                                         linkCheckerQueue: LinkCheckerQueue, feedReaderUpdateService: FeedReaderUpdateService)
extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[FeedReader])

  def processFeed(feed: Feed, loggedInUser: User): Unit = {
    processFeed(feed, loggedInUser, feed.getAcceptancePolicy)
  }

  def processFeed(feed: Feed, readingUser: User, acceptancePolicy: FeedAcceptancePolicy): Unit = {

    def markFeedAsRead(feed: Feed): Future[Unit] = {
      rssfeedNewsitemService.getLatestPublicationDate(feed).map { latestItemDate => // TODO duplicate feed items read
        contentUpdateService.update(feed.copy(
          last_read = Some(DateTime.now.toDate),
          latestItemDate = latestItemDate
        ))
      }
    }

    try {
      log.info("Processing feed: " + feed.title + " using acceptance policy '" + acceptancePolicy + "'. Last read: " + feed.last_read)

      rssfeedNewsitemService.getFeedItemsAndDetailsFor(feed).map { feedItems =>
        feedItems.fold({ l =>
          log.warn("Could new get feed items for feed + '" + feed.title + "':" + l)

        }, { r =>
          val feedNewsitems = r._1
          log.info("Feed contains " + feedNewsitems.size + " items")
          feed.setHttpStatus(if (feedNewsitems.nonEmpty) 200 else -3)
          if (acceptancePolicy.shouldReadFeed) {
            processFeedItems(feed, readingUser, acceptancePolicy, feedNewsitems)
          }
          markFeedAsRead(feed)
          log.info("Done processing feed.")
        })
      }

    } catch {
      case e: Exception =>
        log.error(e, e)
    }
  }

  private def processFeedItems(feed: Feed, feedReaderUser: User, acceptancePolicy: FeedAcceptancePolicy, feedNewsitems: Seq[FeedItem]): Future[Seq[Unit]] = {
    log.info("Accepting feed items")

    val eventualProcessed = feedNewsitems.map { feednewsitem => // TODO new up a new copy before modifying

      val cleanSubmittedItemUrl = urlCleaner.cleanSubmittedItemUrl(feednewsitem.getUrl)
      feednewsitem.setUrl(cleanSubmittedItemUrl)    // TODO do not mutate inputs

      val acceptanceErrors = feedAcceptanceDecider.getAcceptanceErrors(feed, feednewsitem, acceptancePolicy)
      if (acceptanceErrors.isEmpty) {
        log.info("Accepting newsitem: " + feednewsitem.getUrl)
        feedReaderUpdateService.acceptNewsitem(feedReaderUser, feednewsitem, feed).map { acceptedNewsitem =>
          linkCheckerQueue.add(acceptedNewsitem._id.stringify)
        }

      } else {
        log.debug("Not accepting " + feednewsitem.getUrl + " due to acceptance errors: " + acceptanceErrors)
        Future.unit
      }
    }

    Future.sequence(eventualProcessed)
  }

}
