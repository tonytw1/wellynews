package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{HandTaggingService, HibernateResourceDAO}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await

@Component class LoginResourceOwnershipService @Autowired()(resourceDAO: HibernateResourceDAO,
                                                            mongoRepository: MongoRepository, handTaggingService: HandTaggingService)
  extends ReasonableWaits {

  def reassignOwnership(previousOwner: User, newOwner: User) {
    resourceDAO.getOwnedBy(previousOwner, 1000).foreach { resource => // TODO should do all or not at all
      resource.setOwner(newOwner)
      mongoRepository.saveResource(resource)
    }
    handTaggingService.transferVotes(previousOwner, newOwner)

    Await.result(mongoRepository.removeUser(previousOwner), TenSeconds)
  }

}
