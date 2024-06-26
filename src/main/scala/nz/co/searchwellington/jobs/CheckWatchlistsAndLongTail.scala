package nz.co.searchwellington.jobs

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.linkchecking.LinkCheckRequest
import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID

import java.util.Date
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Component class CheckWatchlistsAndLongTail @Autowired()(mongoRepository: MongoRepository, linkCheckerQueue: LinkCheckerQueue)
  extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[CheckWatchlistsAndLongTail])

  @Scheduled(fixedRate = 86400000, initialDelay = 600000)
  def queueWatchlistItems(): Unit = {
    log.info("Queuing watchlist items for checking.")
    val watchlists = mongoRepository.getAllWatchlists.map { watchlists =>
      watchlists.map(w => (w._id, w.last_scanned))
    }
    val queued = enqueue(Seq(watchlists), 1000)
    log.info("Queued watchlists: " + queued.size)
  }

  @Scheduled(cron = "0 */5 * * * *")
  def queueExpiredItems(): Unit = {
    val numberOfItemsToQueue = 100

    log.info("Queuing items")

    def neverScanned = mongoRepository.getNeverScanned(numberOfItemsToQueue)

    def lastScannedOverAMonthAgo = mongoRepository.getNotCheckedSince(DateTime.now.minusMonths(1), numberOfItemsToQueue)

    val queued = enqueue(Seq(neverScanned, lastScannedOverAMonthAgo), numberOfItemsToQueue)
    log.info("Queued: " + queued.flatten.size)
  }

  private def enqueue(selectors: Seq[Future[Seq[(BSONObjectID, Option[Date])]]], numberOfItemsToQueue: Int): Seq[String] = {
    val eventualIdsToQueue = Future.sequence(selectors).map(_.flatten.take(numberOfItemsToQueue)) // TODO this flatten should be redundant

    val eventuallyQueued = eventualIdsToQueue.map { idsToQueue =>
      idsToQueue.foreach { id =>
        queueBsonID(id)
      }
      idsToQueue.map(_._1.stringify)
    }

    Await.result(eventuallyQueued, TenSeconds)
  }

  private def queueBsonID(r: (BSONObjectID, Option[Date])): String = {
    linkCheckerQueue.add(LinkCheckRequest(resourceId = r._1.stringify, lastScanned = r._2))
    r._1.stringify
  }

}
