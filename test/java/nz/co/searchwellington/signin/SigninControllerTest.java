package nz.co.searchwellington.signin;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;

import nz.co.searchwellington.controllers.AnonUserService;
import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.controllers.LoginResourceOwnershipService;
import nz.co.searchwellington.controllers.UrlStack;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.UserRepository;


public class SigninControllerTest {
	
	private @Mock LoggedInUserFilter loggedInUserFilter;
	private @Mock UserRepository userDAO;
	private @Mock AnonUserService anonUserService;
	private @Mock LoginResourceOwnershipService loginResourceOwnershipService;
	private @Mock UrlStack urlStack;
	private @Mock SigninHandler signinHandler;
	
	private MockHttpServletRequest request;
	private @Mock HttpServletResponse response;

	private @Mock Object externalIdentifier;
	private @Mock User existingUser;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	
	@Test
	public void shouldLoginExistingUserIdentifiedBySigninHandler() throws Exception {		
		SigninController controller = new SigninController(loggedInUserFilter, userDAO, anonUserService, loginResourceOwnershipService, urlStack, signinHandler);		

		when(signinHandler.getExternalUserIdentifierFromCallbackRequest(request)).thenReturn(externalIdentifier);
		when(signinHandler.getUserByExternalIdentifier(externalIdentifier)).thenReturn(existingUser);
		
		controller.callback(request, response);
		verify(loggedInUserFilter).setLoggedInUser(request, existingUser);		
	}
	
}
