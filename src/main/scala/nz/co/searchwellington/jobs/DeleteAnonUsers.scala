package nz.co.searchwellington.jobs

import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class DeleteAnonUsers @Autowired()(mongoRepository: MongoRepository) {

  private val log = Logger.getLogger(classOf[DeleteAnonUsers])

  // Delete anon users with no submissions or taggings.
  // Typically these will be spammers who's spam contributions have been deleted
  @Scheduled(fixedRate = 60000, initialDelay = 60000)
  def deleteAnonUsers(): Unit = {
    log.info("Deleting anon users")
    val eventualToExamine = mongoRepository.getAllUsers().map { users =>
      log.info(s"Filtering ${users.size} total users")
      val anonUsers = users.filter(_.profilename.exists(_.startsWith("anon")))
      anonUsers.take(100)
    }
    eventualToExamine.map { anonUsers =>
      anonUsers.map { user =>
        for {
          tagged <- mongoRepository.getResourceIdsByTaggingUser(user)
          owned <- mongoRepository.getResourcesIdsOwnedBy(user)
        } yield {
          log.info(s"Anon user ${user.name} owns $owned.size and has tagged $tagged")
          if (owned.size + tagged.size == 0) {
            log.info(s"Anon user ${user.name} can be deleted")
          }
        }
      }
    }
  }
}
