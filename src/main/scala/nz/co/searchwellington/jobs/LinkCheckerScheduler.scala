package nz.co.searchwellington.jobs

import java.util.Date

import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.repositories.HibernateResourceDAO
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component class LinkCheckerScheduler @Autowired()(resourceDAO: HibernateResourceDAO, linkCheckerQueue: LinkCheckerQueue) {

  private val log = Logger.getLogger(classOf[LinkCheckerScheduler])

  //@Scheduled(fixedRate = 86400000)
  def queueWatchlistItems {
    log.info("Queuing watchlist items for checking.")
    for (resource <- resourceDAO.getAllWatchlists) {
      log.info("Queuing watchlist item for checking: " + resource.title)
      linkCheckerQueue.add(resource.id)
    }
  }

  //@Scheduled(fixedRate = 3600000)
  def queueExpiredItems {
    val numberOfItemsToQueue: Int = 10
    log.info("Queuing items launched within the last 24 hours with but not scanned within the last 4 hours")
    val oneDayAgo: Date = new DateTime(()).minusDays(1).toDate
    val fourHoursAgo: Date = new DateTime(()).minusHours(4).toDate
    for (resource <- resourceDAO.getNotCheckedSince(oneDayAgo, fourHoursAgo, numberOfItemsToQueue)) {
      if (resource.`type` == "N") {
        log.info("Queying recent newsitem for checking: " + resource.title + " - " + resource.last_scanned2)
        linkCheckerQueue.add(resource.id)
      }
    }

    log.info("Queuing " + numberOfItemsToQueue + " items not scanned for more than one month.")
    val oneMonthAgo: Date = new DateTime(()).minusMonths(1).toDate
    for (resource <- resourceDAO.getNotCheckedSince(oneMonthAgo, numberOfItemsToQueue)) {
      log.info("Queuing for scheduled checking: " + resource.title + " - " + resource.last_scanned2)
      linkCheckerQueue.add(resource.id)
    }
  }

}
