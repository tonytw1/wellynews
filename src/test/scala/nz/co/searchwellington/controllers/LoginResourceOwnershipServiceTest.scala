package nz.co.searchwellington.controllers

import org.mockito.Mockito.verify
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.HandTaggingService
import nz.co.searchwellington.repositories.HibernateBackedUserDAO
import nz.co.searchwellington.repositories.HibernateResourceDAO
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.when

class LoginResourceOwnershipServiceTest {
  @Mock val resourceDAO: HibernateResourceDAO = null
  @Mock val userDAO: HibernateBackedUserDAO = null
  @Mock val handTaggingService: HandTaggingService = null
  @Mock val previousOwner: User = null
  @Mock val newOwner: User = null
  private var loginResourceOwnershipService: LoginResourceOwnershipService = null

  @Before def setup {
    MockitoAnnotations.initMocks(this)
    loginResourceOwnershipService = new LoginResourceOwnershipService(resourceDAO, userDAO, handTaggingService)
  }

  @Test
  @throws[Exception]
  def shouldDeletePreviousUserAfterReassigningResources {
    when(resourceDAO.getOwnedBy(previousOwner, 1000)).thenReturn(Seq())
    loginResourceOwnershipService.reassignOwnership(previousOwner, newOwner)
    verify(userDAO).deleteUser(previousOwner)
  }

  @Test
  @throws[Exception]
  def shouldTransferAllTaggingVotesWhenReassigningUser {
    when(resourceDAO.getOwnedBy(previousOwner, 1000)).thenReturn(Seq())
    loginResourceOwnershipService.reassignOwnership(previousOwner, newOwner)
    verify(handTaggingService).transferVotes(previousOwner, newOwner)
  }

}