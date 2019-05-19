package nz.co.searchwellington.repositories

import nz.co.searchwellington.model.{Tag, User}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class HandTaggingService @Autowired() (handTaggingDao: HandTaggingDAO, frontendContentUpdater: FrontendContentUpdater) {

  private val log = Logger.getLogger(classOf[HandTaggingService])

  def clearTaggingsForTag(tag: Tag) {
    log.debug("Clearing tagging votes for tag: " + tag.getName)
    val votesForTag = handTaggingDao.getVotesForTag(tag)
    log.debug(votesForTag.size + " votes will needs to be cleared and the frontend resources updated.")
    votesForTag.foreach { handTagging =>
      handTaggingDao.delete(handTagging)
      // TODO frontendContentUpdater.update(handTagging.getResource)
    }
  }

  def transferVotes(previousOwner: User, newOwner: User) {
    val previousUsersVotes = handTaggingDao.getUsersVotes(previousOwner)
    log.info("Transfering " + previousUsersVotes.size + " vote from user " + previousOwner.getName + " to " + newOwner.getName)
    previousUsersVotes.map { handTagging =>
      // TODO handTagging.setUser(newOwner)
      // frontendContentUpdater.update(handTagging.getResource)
    }
  }

}