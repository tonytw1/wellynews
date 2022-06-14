package nz.co.searchwellington.controllers

import nz.co.searchwellington.model.{User, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.HandTaggingService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, verify, when}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class LoginResourceOwnershipServiceTest {
  private val mongoRepository = mock(classOf[MongoRepository])
  private val contentUpdateService =  mock(classOf[ContentUpdateService])
  private val handTaggingService =  mock(classOf[HandTaggingService])

  private val previousOwner =  mock(classOf[User])
  private val newOwner =  mock(classOf[User])

  private val resourceSubmittedAsPreviousUser = Website(owner = Some(previousOwner._id))

  private val resourcesOwnedByUser = Seq(resourceSubmittedAsPreviousUser._id)

  private val loginResourceOwnershipService = new LoginResourceOwnershipService(mongoRepository, contentUpdateService, handTaggingService)

  @Test
  def shouldDeletePreviousUserAfterReassigningResources(): Unit = {
    when(mongoRepository.getResourcesIdsOwnedBy(previousOwner)).thenReturn(Future.successful(resourcesOwnedByUser))
    when(mongoRepository.getResourceByObjectId(resourceSubmittedAsPreviousUser._id)).thenReturn(Future.successful(Some(resourceSubmittedAsPreviousUser)))
    when(contentUpdateService.update(resourceSubmittedAsPreviousUser)).thenReturn(Future.successful(true))
    when(handTaggingService.transferVotes(previousOwner, newOwner)).thenReturn(Future.successful(true))
    when(mongoRepository.removeUser(previousOwner)).thenReturn(Future.successful(true))

    loginResourceOwnershipService.reassignOwnership(previousOwner, newOwner)

    verify(mongoRepository).removeUser(previousOwner)
  }

  @Test
  def shouldTransferAllTaggingVotesWhenReassigningUser(): Unit = {
    when(mongoRepository.getResourcesIdsOwnedBy(previousOwner)).thenReturn(Future.successful(resourcesOwnedByUser))
    when(mongoRepository.getResourceByObjectId(resourceSubmittedAsPreviousUser._id)).thenReturn(Future.successful(Some(resourceSubmittedAsPreviousUser)))
    when(contentUpdateService.update(resourceSubmittedAsPreviousUser)).thenReturn(Future.successful(true))
    when(handTaggingService.transferVotes(previousOwner, newOwner)).thenReturn(Future.successful(true))
    when(mongoRepository.removeUser(previousOwner)).thenReturn(Future.successful(true))

    loginResourceOwnershipService.reassignOwnership(previousOwner, newOwner)

    verify(handTaggingService).transferVotes(previousOwner, newOwner)
  }

}