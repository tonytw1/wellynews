package nz.co.searchwellington.feeds

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Feed, User}
import nz.co.searchwellington.repositories.HibernateResourceDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

import scala.concurrent.Await

@Component class FeedReaderRunner @Autowired()(feedReader: FeedReader, userDAO: MongoRepository, resourceDAO: HibernateResourceDAO)
  extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[FeedReaderRunner])
  private val FEED_READER_PROFILE_NAME = "feedreader"

  @Scheduled(cron = "0 */10 * * * *")
  def readFeeds {

    def readAllFeeds(feeds: Seq[Feed]): Unit = {
      getFeedReaderUser.map { feedReaderUser =>
        log.info("Reading " + feeds.size + " feeds as user " + feedReaderUser.name)
        feeds.foreach { feed =>
          feedReader.processFeed(feed, feedReaderUser)
        }
      }
    }

    log.info("Running feed reader.")
    readAllFeeds(resourceDAO.getAllFeeds)
    log.info("Finished reading feeds.")
  }

  private def getFeedReaderUser: Option[User] = {
    val feedReaderUser = Await.result(userDAO.getUserByProfilename(FEED_READER_PROFILE_NAME), TenSeconds)
    if (feedReaderUser.isEmpty) {
      log.warn("Feed reader could not run as no user was found with profile name: " + FEED_READER_PROFILE_NAME)
    }
    feedReaderUser
  }

}
