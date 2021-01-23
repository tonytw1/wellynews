package nz.co.searchwellington.jobs

import io.micrometer.core.instrument.MeterRegistry
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Component class LinkCheckerScheduler @Autowired()(mongoRepository: MongoRepository, linkCheckerQueue: LinkCheckerQueue,
                                                   registry: MeterRegistry)
  extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[LinkCheckerScheduler])

  private val queuedCounter = registry.counter("linkcheck_queued")

  //@Scheduled(fixedRate = 86400000)
  def queueWatchlistItems {
    log.info("Queuing watchlist items for checking.")
    Await.result(mongoRepository.getAllWatchlists, TenSeconds).foreach { w =>
      log.info("Queuing watchlist item for checking: " + w.title)
      linkCheckerQueue.add(w._id.stringify)
    }
  }

  @Scheduled(cron = "0 */10 * * * *")
  def queueExpiredItems {
    val numberOfItemsToQueue = 100

    log.info("Queuing items")
    def neverScanned = mongoRepository.getNeverScanned(100)
    def lastScannedOverAMonthAgo = mongoRepository.getNotCheckedSince(DateTime.now.minusMonths(1), numberOfItemsToQueue)
    val selectors = Seq(neverScanned, lastScannedOverAMonthAgo)

    val eventualQueued: Future[Seq[Seq[String]]] = Future.sequence(selectors.map { selector =>
      selector.map { ids =>
        ids.map(queueBsonIDs)
      }
    })

    val queued= Await.result(eventualQueued, TenSeconds)
    log.info("Queued: " + queued.flatten.size)
    queuedCounter.increment(queued.flatten.size)
  }

  private def queueBsonIDs(r: BSONObjectID): String = {
    val stringify = r.stringify
    log.info("Queuing for scheduled checking: " + stringify)
    linkCheckerQueue.add(stringify)
    stringify
  }

}
