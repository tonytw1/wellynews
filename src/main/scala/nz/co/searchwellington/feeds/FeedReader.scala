package nz.co.searchwellington.feeds

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.reading.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, Newsitem, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.tagging.AutoTaggingService
import nz.co.searchwellington.utils.UrlCleaner
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class FeedReader @Autowired()(rssfeedNewsitemService: RssfeedNewsitemService,
                                         feedItemAcceptanceDecider: FeedItemAcceptanceDecider,
                                         urlCleaner: UrlCleaner,
                                         contentUpdateService: ContentUpdateService,
                                         autoTagger: AutoTaggingService,
                                         feedReaderUpdateService: FeedReaderUpdateService)
  extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[FeedReader])

  def processFeed(feed: Feed, loggedInUser: User)(implicit ec: ExecutionContext): Future[Unit] = {
    processFeed(feed, loggedInUser, feed.getAcceptancePolicy)
  }

  def processFeed(feed: Feed, readingUser: User, acceptancePolicy: FeedAcceptancePolicy)(implicit ec: ExecutionContext): Future[Unit] = {
    try {
      log.info("Processing feed: " + feed.title + " using acceptance policy '" + acceptancePolicy + "'. Last read: " + feed.last_read)
      rssfeedNewsitemService.getFeedItemsAndDetailsFor(feed).flatMap { feedItemsFetch =>
        feedItemsFetch.fold({ l =>
          log.warn("Could new get feed items for feed + '" + feed.title + "':" + l)
          Future.successful()

        }, { r =>
          val feedNewsitems = r._1
          log.debug("Feed contains " + feedNewsitems.size + " items")
          val inferredHttpStatus = if (feedNewsitems.nonEmpty) 200 else -3

          val eventuallyAcceptedNewsitems = if (acceptancePolicy.shouldReadFeed) {
            processFeedItems(feed, readingUser, acceptancePolicy, feedNewsitems)
          } else {
            Future.successful(Seq.empty)
          }

          eventuallyAcceptedNewsitems.map { accepted =>
            if (accepted.nonEmpty) {
              log.info("Accepted " + accepted.size + " newsitems from " + feed.title)
            }
            contentUpdateService.update(feed.copy(
              last_read = Some(DateTime.now.toDate),
              latestItemDate = rssfeedNewsitemService.latestPublicationDateOf(feedNewsitems),
              http_status = inferredHttpStatus
            ))
          }
        })
      }

    } catch {
      case e: Exception =>
        log.error(e, e)
        Future.failed(e)
    }
  }

  private def processFeedItems(feed: Feed, feedReaderUser: User, acceptancePolicy: FeedAcceptancePolicy, feedNewsitems: Seq[FeedItem])(implicit ec: ExecutionContext): Future[Seq[Newsitem]] = {

    val eventualProcessed: Seq[Future[Option[Newsitem]]] = feedNewsitems.map { feednewsitem =>

      val withCleanedUrl = feednewsitem.copy(url = urlCleaner.cleanSubmittedItemUrl(feednewsitem.url))
      feedItemAcceptanceDecider.getAcceptanceErrors(withCleanedUrl, acceptancePolicy).flatMap { acceptanceErrors =>
        if (acceptanceErrors.isEmpty) {
          feedReaderUpdateService.acceptFeeditem(feedReaderUser, withCleanedUrl, feed).map { acceptedNewsitem =>
            Some(acceptedNewsitem)
          }

        } else {
          log.info("Not accepting " + feednewsitem.url + " due to acceptance errors: " + acceptanceErrors)
          Future.successful(None)
        }
      }
    }

    Future.sequence(eventualProcessed).map { processed =>
      val accepted = processed.flatten
      log.info("Processed " + processed.size + " and accepted " + accepted.size)
      accepted
    }
  }

}
