package nz.co.searchwellington.controllers

import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.HandTaggingService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Test
import org.mockito.Mockito.{mock, verify, when}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class LoginResourceOwnershipServiceTest {
  private val mongoRepository = mock(classOf[MongoRepository])
  private val handTaggingService =  mock(classOf[HandTaggingService])

  private val previousOwner =  mock(classOf[User])
  private val newOwner =  mock(classOf[User])

  private val resourcesOwnedByUser = Seq.empty  // TODO content

  private val loginResourceOwnershipService = new LoginResourceOwnershipService(mongoRepository, handTaggingService)

  @Test
  @throws[Exception]
  def shouldDeletePreviousUserAfterReassigningResources {
    when(mongoRepository.getResourcesOwnedBy(previousOwner)).thenReturn(Future.successful(resourcesOwnedByUser))

    loginResourceOwnershipService.reassignOwnership(previousOwner, newOwner)

    verify(mongoRepository).removeUser(previousOwner)
  }

  @Test
  @throws[Exception]
  def shouldTransferAllTaggingVotesWhenReassigningUser {
    when(mongoRepository.getResourcesOwnedBy(previousOwner)).thenReturn(Future.successful(resourcesOwnedByUser))

    loginResourceOwnershipService.reassignOwnership(previousOwner, newOwner)

    verify(handTaggingService).transferVotes(previousOwner, newOwner)
  }

}