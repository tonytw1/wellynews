package nz.co.searchwellington.jobs

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Component class LinkCheckerScheduler @Autowired()(mongoRepository: MongoRepository, linkCheckerQueue: LinkCheckerQueue)
  extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[LinkCheckerScheduler])

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
    /*
    log.info("Queuing items launched within the last 24 hours with but not scanned within the last 4 hours")
    val oneDayAgo: Date = new DateTime(()).minusDays(1).toDate
    val fourHoursAgo: Date = new DateTime(()).minusHours(4).toDate
    for (resource <- mongoRepository.getNotCheckedSince(oneDayAgo, fourHoursAgo, numberOfItemsToQueue)) {
      if (resource.`type` == "N") {
        log.info("Queuing recent newsitem for checking: " + resource.title + " - " + resource.last_scanned)
        linkCheckerQueue.add(resource.id)
      }
    }
    */

    log.info("Queuing " + numberOfItemsToQueue + " items not scanned for more than one month.")
    val oneMonthAgo = DateTime.now.minusMonths(1)
    Await.result(mongoRepository.getNotCheckedSince(oneMonthAgo, numberOfItemsToQueue), TenSeconds).foreach { r =>
      log.info("Queuing for scheduled checking: " + r.stringify)
      linkCheckerQueue.add(r.stringify)
    }
  }

}
