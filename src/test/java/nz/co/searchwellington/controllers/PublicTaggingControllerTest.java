package nz.co.searchwellington.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.User;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

public class PublicTaggingControllerTest {
	
	private static final String EXIT_URL = "http://somewhere/beforehere";

	/*
	@Mock AdminRequestFilter adminRequestFilter;
	@Mock AnonUserService anonUserService;
	@Mock LoggedInUserFilter loggedInUserFilter;
	@Mock SubmissionProcessingService submissionProcessingService;
	@Mock UrlStack urlStack;

	@Mock User user;
	User anonUser = new User(name ="Anon");
	@Mock Resource resource;

	MockHttpServletRequest request;
	MockHttpServletResponse response;
	
	PublicTaggingController controller;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		controller = new PublicTaggingController(adminRequestFilter,
				loggedInUserFilter, anonUserService,
				submissionProcessingService, urlStack);
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		request.setAttribute("resource", resource);

		when(loggedInUserFilter.getLoggedInUser()).thenReturn(user);
		when(urlStack.getExitUrlFromStack(request)).thenReturn(EXIT_URL);
	}
	
	
	@Test
	public void shouldProcessTagsOnly() throws Exception {
		controller.tag(request, response);
		verify(submissionProcessingService).processTags(request, resource, user);
		verifyNoMoreInteractions(submissionProcessingService);
	}
	
	
	@Test 
	public void shouldRedirectToUrlStackOnSuccess() throws Exception {		
		ModelAndView mv = controller.tag(request, response);
		verify(urlStack).getExitUrlFromStack(request);
		assertNotNull(mv);
		RedirectView view = (RedirectView) mv.getView();
		assertEquals(view.getUrl(), EXIT_URL);
	}
	
	@Test
	public void shouldAssignAnonUserIfNoOneIsLoggedIn() throws Exception {
		when(loggedInUserFilter.getLoggedInUser()).thenReturn(null);
		when(anonUserService.createAnonUser()).thenReturn(anonUser);
		controller.tag(request, response);
		verify(submissionProcessingService).processTags(request, resource, anonUser);
	}
	*/

}
