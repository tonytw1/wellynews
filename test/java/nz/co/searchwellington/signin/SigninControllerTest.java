package nz.co.searchwellington.signin;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nz.co.searchwellington.controllers.AnonUserService;
import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.controllers.LoginResourceOwnershipService;
import nz.co.searchwellington.controllers.UrlStack;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.UserRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


public class SigninControllerTest {
	
	private @Mock LoggedInUserFilter loggedInUserFilter;
	private @Mock UserRepository userDAO;
	private @Mock AnonUserService anonUserService;
	private @Mock LoginResourceOwnershipService loginResourceOwnershipService;
	private @Mock UrlStack urlStack;
	private @Mock SigninHandler signinHandler;
	
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	private @Mock Object externalIdentifier;
	private @Mock Object seenForTheFirstTimexternalIdentifier;
	
	private @Mock User existingUser;
	private @Mock User newUser;
	private @Mock User anonUser;
	
	SigninController controller;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(anonUser.isUnlinkedAnonAccount()).thenReturn(true);
		controller = new SigninController(loggedInUserFilter, userDAO, anonUserService, loginResourceOwnershipService, urlStack, signinHandler);
	}
	
	
	@Test
	public void shouldLoginExistingUserIdentifiedBySigninHandler() throws Exception {		
		when(signinHandler.getExternalUserIdentifierFromCallbackRequest(request)).thenReturn(externalIdentifier);
		when(signinHandler.getUserByExternalIdentifier(externalIdentifier)).thenReturn(existingUser);
		
		controller.callback(request, response);
		verify(loggedInUserFilter).setLoggedInUser(request, existingUser);		
	}
	
	
	@Test
	public void testShouldCreateNewUserIfExternalIdentifierIsPreviouslyUnknown() throws Exception {
		when(signinHandler.getExternalUserIdentifierFromCallbackRequest(request)).thenReturn(seenForTheFirstTimexternalIdentifier);
		when(signinHandler.getUserByExternalIdentifier(seenForTheFirstTimexternalIdentifier)).thenReturn(null);		
		when(anonUserService.createAnonUser()).thenReturn(newUser);
		
		controller.callback(request, response);		
		verify(userDAO).saveUser(newUser);
		verify(loggedInUserFilter).setLoggedInUser(request, newUser);
	}
	
	
	@Test
	public void testShouldMergeIntoExistingAccountWhenAnonUserSignsInWithKnownExternalIdentifier() throws Exception {
		when(loggedInUserFilter.getLoggedInUser()).thenReturn(anonUser);
		when(signinHandler.getExternalUserIdentifierFromCallbackRequest(request)).thenReturn(externalIdentifier);
		when(signinHandler.getUserByExternalIdentifier(externalIdentifier)).thenReturn(existingUser);
		
		controller.callback(request, response);
		verify(loggedInUserFilter).setLoggedInUser(request, existingUser);
		verify(loginResourceOwnershipService).reassignOwnership(anonUser, existingUser);
		verify(userDAO).deleteUser(anonUser);
	}
	
}
