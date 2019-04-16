package nz.co.searchwellington.feeds

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Feed, User}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

import scala.concurrent.duration.{Duration, SECONDS}
import scala.concurrent.{Await, ExecutionContext, Future}

@Component class FeedReaderRunner @Autowired()(feedReader: FeedReader, mongoRepository: MongoRepository,
                                               feedReaderTaskExecutor: TaskExecutor)  // TODO named bean
  extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[FeedReaderRunner])
  private val FEED_READER_PROFILE_NAME = "feedreader"

  implicit val executionContext = ExecutionContext.fromExecutor(feedReaderTaskExecutor)

  @Scheduled(cron = "0 */10 * * * *")
  def readFeeds {

    def readAllFeeds(feeds: Seq[Feed]): Unit = {
      getFeedReaderUser.map { feedReaderUser =>
        log.info("Reading " + feeds.size + " feeds as user " + feedReaderUser.name)
        Future.sequence(feeds.map { feed =>
          feedReader.processFeed(feed, feedReaderUser)
        }).map { _ =>
          log.info("Finished reading feeds")
        }
      }.getOrElse {
        log.warn("Feed reader could not run as no user was found with profile name: " + FEED_READER_PROFILE_NAME)
      }
    }

    log.info("Running feed reader.")
    readAllFeeds(Await.result(mongoRepository.getAllFeeds(), Duration(10, SECONDS)))
    log.info("Finished reading feeds.")
  }

  private def getFeedReaderUser: Option[User] = {
    Await.result(mongoRepository.getUserByProfilename(FEED_READER_PROFILE_NAME), TenSeconds)
  }

}
