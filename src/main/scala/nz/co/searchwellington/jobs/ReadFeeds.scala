package nz.co.searchwellington.jobs

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.FeedReader
import nz.co.searchwellington.model.{Feed, User}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}

@Component class ReadFeeds @Autowired()(feedReader: FeedReader,
                                        mongoRepository: MongoRepository,
                                        @Qualifier("feedReaderTaskExecutor") feedReaderTaskExecutor: TaskExecutor)
  extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[ReadFeeds])
  private val FEED_READER_PROFILE_NAME = "feedreader"

  @Scheduled(cron = "0 */10 * * * *")
  def readFeeds(): Unit = {
    implicit val executionContext: ExecutionContextExecutor = ExecutionContext.fromExecutor(feedReaderTaskExecutor)

    def getFeedReaderUser: Future[Option[User]] = mongoRepository.getUserByProfilename(FEED_READER_PROFILE_NAME)

    def readAllFeeds(feeds: Seq[Feed]): Future[Boolean] = {
      getFeedReaderUser.map { maybyFeedUser =>
        maybyFeedUser.map { feedReaderUser =>
          log.info("Reading " + feeds.size + " feeds as user " + feedReaderUser.name)
          feeds.foreach { feed =>
            try {
              Await.result(feedReader.processFeed(feed, feedReaderUser), TenSeconds)
            } catch {
              case e: Exception =>
                log.error("Error reading feed: " + feed, e)
            }
          }
          true

        }.getOrElse {
          log.warn("Feed reader could not run as no user was found with profile name: " + FEED_READER_PROFILE_NAME)
          false
        }
      }
    }

    log.info("Running feed reader.")
    mongoRepository.getAllFeeds().flatMap { feeds =>
      readAllFeeds(feeds)
    }.map { _ =>
      log.info("Finished reading feeds.")
    }
  }

}