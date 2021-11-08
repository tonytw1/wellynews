package nz.co.searchwellington.feeds.whakaoko

import com.google.common.base.Strings
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{Await, ExecutionContext}

@Component class WhakaokoFeedSyncService @Autowired()(mongoReposity: MongoRepository, whakaokoService: WhakaokoService) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[WhakaokoFeedSyncService])

  // @Scheduled(fixedRate = 3600000)
  def run {
    //registerFeedsWithWhakaoko(Await.result(mongoReposity.getAllFeeds, TenSeconds))
  }

  private def registerFeedsWithWhakaoko(feeds: Seq[Feed])(implicit ec: ExecutionContext) {
    log.info("Registering feeds with whakaoko")
    feeds.map { feed =>
      if (!Strings.isNullOrEmpty(feed.page)) {
        log.info("Registering feed with whakaoko: " + feed.title)
        Await.result(whakaokoService.createFeedSubscription(feed.page), TenSeconds).map { createdSubscriptionId =>
          log.info("Created whakaoko feed: " + createdSubscriptionId)
        }
      }

      /*
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
       */

    }

    log.info("Finished registering feeds with whakaoko")
  }

}
