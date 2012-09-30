package nz.co.searchwellington.controllers;

import static org.mockito.Mockito.verify;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.HandTaggingService;
import nz.co.searchwellington.repositories.HibernateResourceDAO;
import nz.co.searchwellington.repositories.HibernateBackedUserDAO;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LoginResourceOwnershipServiceTest {

	@Mock HibernateResourceDAO resourceDAO;
	@Mock HibernateBackedUserDAO userDAO;
	@Mock HandTaggingService handTaggingService;
	
	@Mock User previousOwner;
	@Mock User newOwner;
	
	private LoginResourceOwnershipService loginResourceOwnershipService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		loginResourceOwnershipService = new LoginResourceOwnershipService(resourceDAO, userDAO, handTaggingService);
	}
	
	@Test
	public void shouldDeletePreviousUserAfterReassigningResources() throws Exception {
		loginResourceOwnershipService.reassignOwnership(previousOwner, newOwner);
		verify(userDAO).deleteUser(previousOwner);
	}
	
	@Test
	public void shouldTransferAllTaggingVotesWhenReassigningUser() throws Exception {
		loginResourceOwnershipService.reassignOwnership(previousOwner, newOwner);
		verify(handTaggingService).transferVotes(previousOwner, newOwner);
	}
	
}
