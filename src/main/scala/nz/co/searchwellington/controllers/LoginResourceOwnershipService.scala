package nz.co.searchwellington.controllers

import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.{HandTaggingService, HibernateBackedUserDAO, HibernateResourceDAO}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class LoginResourceOwnershipService @Autowired()(var resourceDAO: HibernateResourceDAO, var userDAO: HibernateBackedUserDAO, var handTaggingService: HandTaggingService) {

  def reassignOwnership(previousOwner: User, newOwner: User) {
    resourceDAO.getOwnedBy(previousOwner, 1000).map { resource => // TODO should do all or not at all
      resource.setOwner(newOwner)
      resourceDAO.saveResource(resource)
    }

    handTaggingService.transferVotes(previousOwner, newOwner)
    userDAO.deleteUser(previousOwner)
  }

}
