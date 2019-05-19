package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Tag, User}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Component class HandTaggingService @Autowired()(handTaggingDao: HandTaggingDAO, frontendContentUpdater: FrontendContentUpdater,
                                                 mongoRepository: MongoRepository) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[HandTaggingService])

  def clearTaggingsForTag(tag: Tag) {
    log.info("Clearing tagging votes for tag: " + tag.getName)
    val resourceIdsTaggedWithTag = Await.result(mongoRepository.getResourceIdsByTag(tag), TenSeconds)
    log.info(resourceIdsTaggedWithTag.size + " votes will needs to be cleared and the frontend resources updated.")

    val eventualMaybeResources  = Future.sequence(resourceIdsTaggedWithTag.map { rid =>
      mongoRepository.getResourceByObjectId(rid)
    }).map( _.flatten)

    Await.result(eventualMaybeResources, TenSeconds).map { taggedResource =>
      val updatedResource = handTaggingDao.deleteTagFromResource(tag, taggedResource)
      mongoRepository.saveResource(updatedResource)
      frontendContentUpdater.update(updatedResource)
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