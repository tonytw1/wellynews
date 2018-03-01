package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.{Feed, User}
import nz.co.searchwellington.repositories.{HibernateBackedUserDAO, HibernateResourceDAO}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component class FeedReaderRunner @Autowired()(feedReader: FeedReader, userDAO: HibernateBackedUserDAO, resourceDAO: HibernateResourceDAO) {

  private val log = Logger.getLogger(classOf[FeedReaderRunner])
  private val FEED_READER_PROFILE_NAME = "feedreader"

  //@Scheduled(fixedRate = 1200000)
  def readFeeds {
    log.info("Running feed reader.")
    readAllFeeds(resourceDAO.getAllFeeds)
    log.info("Finished reading feeds.")
  }

  def readAllFeeds(feeds: Seq[Feed]) {
    getFeedReaderUser.map { feedReaderUser =>
      feeds.map { feed =>
        feedReader.processFeed(feed.id, feedReaderUser)
      }
    }
  }

  private def getFeedReaderUser: Option[User] = {
    val feedReaderUser = userDAO.getUserByProfileName(FEED_READER_PROFILE_NAME)
    if (feedReaderUser.isEmpty) {
      log.warn("Feed reader could not run as no user was found with profile name: " + FEED_READER_PROFILE_NAME)
    }
    feedReaderUser
  }

}
