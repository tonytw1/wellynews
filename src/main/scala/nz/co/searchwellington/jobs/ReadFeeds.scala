package nz.co.searchwellington.jobs

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.reading.ReadFeedRequest
import nz.co.searchwellington.model.{Feed, User}
import nz.co.searchwellington.queues.ReadFeedQueue
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class ReadFeeds @Autowired()(mongoRepository: MongoRepository,
                                        readFeedQueue: ReadFeedQueue)
  extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[ReadFeeds])

  private val FEED_READER_PROFILE_NAME = "feedreader"

  // Disabled in favour of Kafka message queue
  //@Scheduled(cron = "0 */10 * * * *")
  def readFeeds(): Unit = {

    def queueAllFeeds(feeds: Seq[Feed], asUser: User): Unit = {
      feeds.foreach { feed =>
        readFeedQueue.add(ReadFeedRequest(feed._id.stringify, asUser._id.stringify, None, feed.last_read))
      }
    }

    log.info("Queuing feeds for reading.")
    val eventualMaybeEventualUnit: Future[Unit] = mongoRepository.getUserByProfilename(FEED_READER_PROFILE_NAME).flatMap { maybeFeedReaderUser =>
      maybeFeedReaderUser.map { feedReaderUser =>
        mongoRepository.getAllFeeds().map(feeds => queueAllFeeds(feeds, feedReaderUser))
      }.getOrElse{
        log.warn(s"Could not find feed reader user: $FEED_READER_PROFILE_NAME; no feeds queued for reading.")
        Future.successful[Unit]()
      }
    }

    eventualMaybeEventualUnit.map{ _ =>
      log.info("Finished queuing feeds.")
    }
  }

}
