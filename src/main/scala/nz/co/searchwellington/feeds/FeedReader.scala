package nz.co.searchwellington.feeds

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.feeds.whakaoko.{WhakaokoFeedReader, WhakaokoService}
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, Resource, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.urls.UrlCleaner
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.Date
import scala.concurrent.{Await, ExecutionContext, Future}

@Component class FeedReader @Autowired()(feedItemAcceptanceDecider: FeedItemAcceptanceDecider,
                                         urlCleaner: UrlCleaner,
                                         contentUpdateService: ContentUpdateService,
                                         feedReaderUpdateService: FeedReaderUpdateService,
                                         whakaokoFeedReader: WhakaokoFeedReader,
                                         whakaokoService: WhakaokoService) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[FeedReader])

  def processFeed(feed: Feed, loggedInUser: User)(implicit ec: ExecutionContext): Future[Unit] = {
    processFeed(feed, loggedInUser, feed.getAcceptancePolicy)
  }

  def processFeed(feed: Feed, readingUser: User, acceptancePolicy: FeedAcceptancePolicy)(implicit ec: ExecutionContext): Future[Unit] = {
    try {
      log.info(s"Processing feed: ${feed.title.getOrElse(feed.page)} using acceptance policy $acceptancePolicy. Last read: " + feed.last_read.getOrElse(""))

      // Attempt to back fill missing whakaoko subscriptions
      if (feed.whakaokoSubscription.isEmpty) {
        val eventualMaybeEventualBoolean: Future[Boolean] = whakaokoService.getSubscriptions().flatMap { subscriptions =>
          subscriptions.find(s => s.url == feed.page).map { s =>
            log.debug("Found subscription to attached to feed: " + s)
            feed.whakaokoSubscription = Some(s.id)
            contentUpdateService.update(feed)
          }.getOrElse {
            Future.successful(false)
          }
        }
        val backfillOutcome = Await.result(eventualMaybeEventualBoolean, TenSeconds)
        log.warn("Feed backfill outcome: " + backfillOutcome)
      }

      // Sync feed names
      val eventualSubscriptionNameSync = feed.whakaokoSubscription.flatMap { subscriptionId =>
        feed.title.map { title =>
          whakaokoService.updateSubscriptionName(subscriptionId, title)
        }
      }.getOrElse(Future.successful(Unit))
      Await.result(eventualSubscriptionNameSync, TenSeconds)

      whakaokoFeedReader.fetchFeedItems(feed).flatMap { feedItemsFetch =>
        feedItemsFetch.fold({ l =>
          log.warn("Could new get feed items for feed + '" + feed.title + "':" + l)
          Future.successful()

        }, { r =>
          val feedNewsitems = r._1
          log.debug("Feed contains " + feedNewsitems._1.size + " items from " + feedNewsitems._2 + " total items")
          val inferredHttpStatus = if (feedNewsitems._1.nonEmpty) 200 else -3

          val eventuallyAcceptedNewsitems = if (acceptancePolicy.shouldReadFeed) {
            processFeedItems(feed, readingUser, acceptancePolicy, feedNewsitems._1)
          } else {
            Future.successful(Seq.empty)
          }

          eventuallyAcceptedNewsitems.flatMap { accepted =>
            if (accepted.nonEmpty) {
              log.info("Accepted " + accepted.size + " newsitems from " + feed.title)
            }

            contentUpdateService.update(feed.copy(
              last_read = Some(DateTime.now.toDate),
              latestItemDate = latestPublicationDateOf(feedNewsitems._1),
              http_status = inferredHttpStatus,
              whakaokoSubscription = Some(r._2.id)
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
  }

  private def processFeedItems(feed: Feed, feedReaderUser: User, acceptancePolicy: FeedAcceptancePolicy, feedNewsitems: Seq[FeedItem])(implicit ec: ExecutionContext): Future[Seq[Resource]] = {
    val eventualProcessed: Seq[Future[Option[Resource]]] = feedNewsitems.map { feednewsitem =>
      val withCleanedUrl = feednewsitem.copy(url = urlCleaner.cleanSubmittedItemUrl(feednewsitem.url))
      feedItemAcceptanceDecider.getAcceptanceErrors(withCleanedUrl, acceptancePolicy).flatMap { acceptanceErrors =>
        if (acceptanceErrors.isEmpty) {
          feedReaderUpdateService.acceptFeeditem(feedReaderUser, withCleanedUrl, feed).map { acceptedNewsitem =>
            Some(acceptedNewsitem)
          }.recover {
            case e: Exception =>
              log.error("Error while accepting feeditem", e)
              None
          }

        } else {
          log.debug("Not accepting " + withCleanedUrl.url + " due to acceptance errors: " + acceptanceErrors.mkString(", "))
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
