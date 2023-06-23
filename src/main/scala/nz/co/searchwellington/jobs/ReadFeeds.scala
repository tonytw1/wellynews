package nz.co.searchwellington.jobs

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.reading.ReadFeedRequest
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.queues.ReadFeedQueue
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global

@Component class ReadFeeds @Autowired()(mongoRepository: MongoRepository,
                                        readFeedQueue: ReadFeedQueue)
  extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[ReadFeeds])

  @Scheduled(cron = "0 */10 * * * *")
  def readFeeds(): Unit = {

    def queueAllFeeds(feeds: Seq[Feed]): Unit = {
      feeds.foreach { feed =>
        readFeedQueue.add(ReadFeedRequest(feed._id.stringify, feed.last_read))
      }
    }

    log.info("Queuing feeds for reading.")
    mongoRepository.getAllFeeds().map(queueAllFeeds).map { _ =>
      log.info("Finished queuing feeds.")
    }
  }

}
