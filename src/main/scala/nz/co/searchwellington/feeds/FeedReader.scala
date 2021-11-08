package nz.co.searchwellington.feeds

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.WhakaokoFeedReader
import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, Resource, User}
import nz.co.searchwellington.modification.ContentUpdateService
import org.apache.log4j.Logger
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

  private val log = Logger.getLogger(classOf[FeedReader])

  def processFeed(feed: Feed, loggedInUser: User)(implicit ec: ExecutionContext): Future[Unit] = {
    processFeed(feed, loggedInUser, feed.getAcceptancePolicy)
  }

  def processFeed(feed: Feed, readingUser: User, acceptancePolicy: FeedAcceptancePolicy)(implicit ec: ExecutionContext): Future[Unit] = {
    if (acceptancePolicy.shouldReadFeed) {
      try {
        log.info(s"Processing feed: ${feed.title.getOrElse(feed.page)} using acceptance policy $acceptancePolicy. Last read: " + feed.last_read.getOrElse(""))
        whakaokoFeedReader.fetchFeedItems(feed).flatMap { feedItemsFetch =>
          feedItemsFetch.fold({ l =>
            log.warn("Could new get feed items for feed + '" + feed.title + "':" + l)
            Future.successful()

          }, { feedNewsitems =>
            log.debug("Feed contains " + feedNewsitems._1.size + " items from " + feedNewsitems._2 + " total items")
            val inferredHttpStatus = if (feedNewsitems._1.nonEmpty) 200 else -3

            val eventuallyAcceptedNewsitems = processFeedItems(feed, readingUser, acceptancePolicy, feedNewsitems._1)
            eventuallyAcceptedNewsitems.flatMap { accepted =>
              if (accepted.nonEmpty) {
                log.info("Accepted " + accepted.size + " newsitems from " + feed.title)
              }

              contentUpdateService.update(feed.copy(
                last_read = Some(DateTime.now.toDate),
                latestItemDate = latestPublicationDateOf(feedNewsitems._1),
                http_status = inferredHttpStatus
              )).map { _ =>
                Unit
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
      Future.successful(Unit)
    }
  }

  private def processFeedItems(feed: Feed, feedReaderUser: User, acceptancePolicy: FeedAcceptancePolicy, feedItems: Seq[FeedItem])(implicit ec: ExecutionContext): Future[Seq[Resource]] = {
    val newsItems = feedItems.map(i => feeditemToNewsItemService.makeNewsitemFromFeedItem(i, feed))
    val eventualProcessed = newsItems.map { newsitem =>
      feedItemAcceptanceDecider.getAcceptanceErrors(newsitem, acceptancePolicy).flatMap { acceptanceErrors =>
        if (acceptanceErrors.isEmpty) {
          feedReaderUpdateService.acceptFeeditem(feedReaderUser, newsitem, feed).map { acceptedNewsitem =>
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
