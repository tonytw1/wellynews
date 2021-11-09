package nz.co.searchwellington.jobs

import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global

@Component class DeleteAnonUsers @Autowired()(mongoRepository: MongoRepository) {

  private val log = Logger.getLogger(classOf[DeleteAnonUsers])

  // Delete users with no submissions or taggings.
  // Typically these will be spammers who's spam contributions have been deleted
  @Scheduled(fixedRate = 600000, initialDelay = 600000)
  def deleteAnonUsers(): Unit = {
    def nonAdmins(user: User) = {
      !user.admin
    }

    log.info("Deleting users with no submissions or taggings")
    val eventualUsersToExamine = mongoRepository.getAllUsers().map { users =>
      log.info(s"Filtering ${users.size} total users")
      users.filter(nonAdmins).take(1000)
    }
    eventualUsersToExamine.map { users =>
      users.map { user =>
        for {
          tagged <- mongoRepository.getResourceIdsByTaggingUser(user)
          owned <- mongoRepository.getResourcesIdsOwnedBy(user)
        } yield {
          val total = owned.size + tagged.size
          log.info(s"User ${user.profilename.getOrElse(user._id)} owns ${owned.size} and has tagged ${tagged.size} for $total submissions.")
          if (total == 0) {
            log.info(s"User ${user.profilename.getOrElse(user._id)} has $total submissions and can be deleted")
            mongoRepository.removeUser(user)
          }
        }
      }
    }
  }

}
