package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.HandTaggingService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await

@Component class LoginResourceOwnershipService @Autowired()(mongoRepository: MongoRepository, handTaggingService: HandTaggingService)
  extends ReasonableWaits {

  def reassignOwnership(previousOwner: User, newOwner: User) {
    Await.result(mongoRepository.getResourcesOwnedBy(previousOwner), TenSeconds).foreach { resource => // TODO should do all or not at all
      //resource.setOwner(newOwner)
      //mongoRepository.saveResource(resource)
    }
    handTaggingService.transferVotes(previousOwner, newOwner)

    mongoRepository.removeUser(previousOwner)
  }

}
