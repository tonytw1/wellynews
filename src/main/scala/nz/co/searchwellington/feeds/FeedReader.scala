package nz.co.searchwellington.feeds

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.WhakaokoFeedReader
import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, Resource, User}
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
                                         whakaokoFeedReader: WhakaokoFeedReader,
                                         feeditemToNewsItemService: FeeditemToNewsitemService) extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[FeedReader])

  def processFeed(feed: Feed, loggedInUser: User)(implicit ec: ExecutionContext): Future[Int] = {
    processFeed(feed, loggedInUser, feed.getAcceptancePolicy)
  }

  def processFeed(feed: Feed, readingUser: User, acceptancePolicy: FeedAcceptancePolicy)(implicit ec: ExecutionContext): Future[Int] = {
    if (acceptancePolicy.shouldReadFeed) {
      try {
        log.debug(s"Processing feed: ${feed.title} using acceptance policy $acceptancePolicy. Last read: " + feed.last_read.getOrElse(""))
        whakaokoFeedReader.fetchFeedItems(feed).flatMap { feedItemsFetch =>
          feedItemsFetch.fold({ l =>
            log.warn("Could not fetch feed items for feed + '" + feed.title + "':" + l)
            Future.successful(0)

          }, { feedNewsitems =>
            log.debug("Feed contains " + feedNewsitems._1.size + " items from " + feedNewsitems._2 + " total items")
            val inferredHttpStatus = if (feedNewsitems._1.nonEmpty) 200 else -3

            val eventuallyAcceptedNewsitems = processFeedItems(feed, readingUser, acceptancePolicy, feedNewsitems._1)
            eventuallyAcceptedNewsitems.flatMap { acceptedNewsitems =>
              if (acceptedNewsitems.nonEmpty) {
                log.info("Accepted " + acceptedNewsitems.size + " newsitems from " + feed.title)
              }

              contentUpdateService.update(feed.copy(
                last_read = Some(DateTime.now.toDate),
                latestItemDate = latestPublicationDateOf(feedNewsitems._1),
                http_status = inferredHttpStatus
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

    } else {
      Future.successful(0)
    }
  }

  private def processFeedItems(feed: Feed, feedReaderUser: User, acceptancePolicy: FeedAcceptancePolicy, feedItems: Seq[FeedItem])(implicit ec: ExecutionContext): Future[Seq[Resource]] = {
    val eventualProcessed = feedItems.map { feedItem =>
      val newsitem = feeditemToNewsItemService.makeNewsitemFromFeedItem(feedItem, feed)
      feedItemAcceptanceDecider.getAcceptanceErrors(newsitem, acceptancePolicy).flatMap { acceptanceErrors =>
        if (acceptanceErrors.isEmpty) {
          feedReaderUpdateService.acceptFeeditem(feedReaderUser, newsitem, feed,
            feedItem.categories.getOrElse(Seq.empty)
          ).map { acceptedNewsitem =>
            Some(acceptedNewsitem)
          }.recover {
            case e: Exception =>
              log.error("Error while accepting feeditem", e)
              None
          }
        } else {
          log.debug("Not accepting " + newsitem.page + " due to acceptance errors: " + acceptanceErrors.mkString(", "))
          Future.successful(None)
        }
      }
    }

    Future.sequence(eventualProcessed).map { processed =>
      val accepted = processed.flatten
      log.debug("Processed feed items " + processed.size + " and accepted " + accepted.size)
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
