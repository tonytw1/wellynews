package nz.co.searchwellington.feeds

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.WhakaokoFeedReader
import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, HttpStatus, Resource, User}
import nz.co.searchwellington.modification.ContentUpdateService
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.Date
import scala.concurrent.{ExecutionContext, Future}

@Component class FeedReader @Autowired()(feedItemAcceptanceDecider: FeedItemAcceptanceDecider,
                                         contentUpdateService: ContentUpdateService,
                                         feedReaderUpdateService: FeedReaderUpdateService,
                                         whakaokoFeedReader: WhakaokoFeedReader) extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[FeedReader])

  def processFeed(feed: Feed, readingUser: User, overriddenAcceptancePolicy: Option[FeedAcceptancePolicy] = None)(implicit ec: ExecutionContext, currentSpan: Span): Future[Int] = {
    try {
      val acceptancePolicy = overriddenAcceptancePolicy.getOrElse(feed.acceptance)
      log.debug(s"Processing feed: ${feed.title} using acceptance policy $acceptancePolicy. Last read: " + feed.last_read.getOrElse(""))
      whakaokoFeedReader.fetchFeedItems(feed).flatMap { feedItemsFetch =>
        feedItemsFetch.fold({ l =>
          log.warn("Could not fetch feed items for feed + '" + feed.title + "':" + l)
          Future.successful(0)

        }, { fetchedFeedItems =>
          val (feedNewsitems, total) = fetchedFeedItems
          log.debug("Feed contains " + feedNewsitems.size + " items from " + total + " total items")

          val eventuallyAcceptedNewsitems = {
            if (acceptancePolicy.shouldAcceptFeedItems) {
              processFeedItems(feed, readingUser, acceptancePolicy, feedNewsitems)
            } else {
              Future.successful(Seq.empty)
            }
          }

          eventuallyAcceptedNewsitems.flatMap { acceptedNewsitems =>
            if (acceptedNewsitems.nonEmpty) {
              log.info("Accepted " + acceptedNewsitems.size + " newsitems from " + feed.title)
            }

            val inferredHttpStatus = if (feedNewsitems.nonEmpty) 200 else -3

            val newHttpStatus = feed.httpStatus.map(_.copy(status = inferredHttpStatus)).getOrElse(HttpStatus(inferredHttpStatus, redirecting = false))

            contentUpdateService.update(feed.copy(
              last_read = Some(DateTime.now.toDate),
              latestItemDate = latestPublicationDateOf(feedNewsitems),
              httpStatus = Some(newHttpStatus)
            )).map { _ =>
              acceptedNewsitems.size
            }
          }
        })
      }

    } catch {
      case e: Exception =>
        log.error(e, e)
        Future.failed(e)
    }
  }

  private def processFeedItems(feed: Feed, feedReaderUser: User, acceptancePolicy: FeedAcceptancePolicy, feedItems: Seq[FeedItem])(implicit ec: ExecutionContext): Future[Seq[Resource]] = {

    val eventualMaybeAccepted = feedItems.map { feedItem =>
      feedItemAcceptanceDecider.getAcceptanceErrors(feedItem, acceptancePolicy).flatMap { acceptanceErrors =>
        if (acceptanceErrors.isEmpty) {
          feedReaderUpdateService.acceptFeeditem(feedReaderUser, feedItem, feed, feedItem.categories.getOrElse(Seq.empty)).map { acceptedNewsitem =>
            acceptedNewsitem
          }.recover {
            case e: Exception =>
              log.error("Error while accepting feeditem", e)
              None
          }
        } else {
          log.debug("Not accepting " + feedItem.url + " due to acceptance errors: " + acceptanceErrors.mkString(", "))
          Future.successful(None)
        }
      }
    }

    Future.sequence(eventualMaybeAccepted).map { maybeAccepted =>
      val accepted = maybeAccepted.flatten
      log.debug("Processed feed items " + maybeAccepted.size + " and accepted " + accepted.size)
      accepted
    }
  }

  private def latestPublicationDateOf(feedItems: Seq[FeedItem]): Option[Date] = {
    val publicationDates = feedItems.flatMap(fi => fi.date.map(_.toDate))
    if (publicationDates.nonEmpty) {
      Some(publicationDates.max)
    } else {
      None
    }
  }

}
