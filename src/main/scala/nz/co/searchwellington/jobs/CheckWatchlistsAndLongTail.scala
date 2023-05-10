package nz.co.searchwellington.jobs

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Component class CheckWatchlistsAndLongTail @Autowired()(mongoRepository: MongoRepository, linkCheckerQueue: LinkCheckerQueue)
  extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[CheckWatchlistsAndLongTail])

  @Scheduled(fixedRate = 86400000, initialDelay = 600000)
  def queueWatchlistItems(): Unit = {
    log.info("Queuing watchlist items for checking.")
    val eventuallyQueued = mongoRepository.getAllWatchlists.map { watchlists =>
      watchlists.map(_._id).map(queueBsonIDs)
    }
    val queued = Await.result(eventuallyQueued, TenSeconds)
    log.info("Queued watchlists: " + queued.size)
  }

  @Scheduled(cron = "0 */5 * * * *")
  def queueExpiredItems(): Unit = {
    val numberOfItemsToQueue = 100

    log.info("Queuing items")
    def neverScanned = mongoRepository.getNeverScanned(numberOfItemsToQueue)
    def lastScannedOverAMonthAgo = mongoRepository.getNotCheckedSince(DateTime.now.minusWeeks(1), numberOfItemsToQueue)
    val selectors = Seq(neverScanned, lastScannedOverAMonthAgo)

    val eventuallyQueued = Future.sequence(selectors.map { selector =>
      selector.map { ids =>
        ids.take(numberOfItemsToQueue).map(queueBsonIDs)
      }
    })

    val queued= Await.result(eventuallyQueued, TenSeconds)
    log.info("Queued: " + queued.flatten.size)
  }

  private def queueBsonIDs(r: BSONObjectID): String = {
    val stringify = r.stringify
    log.info("Queuing for scheduled checking: " + stringify)
    linkCheckerQueue.add(stringify)
    stringify
  }

}
