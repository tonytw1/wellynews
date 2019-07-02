package nz.co.searchwellington.feeds

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Feed, User}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class FeedReaderRunner @Autowired()(feedReader: FeedReader, mongoRepository: MongoRepository,
                                               feedReaderTaskExecutor: TaskExecutor) // TODO named bean
  extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[FeedReaderRunner])
  private val FEED_READER_PROFILE_NAME = "feedreader"

  implicit val executionContext = ExecutionContext.fromExecutor(feedReaderTaskExecutor)

  @Scheduled(cron = "0 */10 * * * *")
  def readFeeds {

    def readAllFeeds(feeds: Seq[Feed]): Future[Boolean] = {
      getFeedReaderUser.flatMap { maybyFeedUser =>
        maybyFeedUser.map { feedReaderUser =>
          log.info("Reading " + feeds.size + " feeds as user " + feedReaderUser.name)
          Future.sequence(feeds.map { feed =>
            feedReader.processFeed(feed, feedReaderUser)
          }).map { _ =>
            log.info("Finished reading feeds")
            true
          }
        }.getOrElse {
          log.warn("Feed reader could not run as no user was found with profile name: " + FEED_READER_PROFILE_NAME)
          Future.successful(false)
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

  private def getFeedReaderUser: Future[Option[User]] = mongoRepository.getUserByProfilename(FEED_READER_PROFILE_NAME)

}
