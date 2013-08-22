package nz.co.searchwellington.controllers.profiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import nz.co.searchwellington.controllers.CommonModelObjectsService;
import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.repositories.HibernateBackedUserDAO;
import nz.co.searchwellington.urls.UrlBuilder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

public class ProfileControllerTest {

	private static final String VALID_PROFILE_NAME = "tonytw1";
	private static final String INVALID_PROFILE_NAME = "tony-tw1";
	
	@Mock HibernateBackedUserDAO userDao;
	@Mock LoggedInUserFilter loggedInUserFilter;
	@Mock UrlBuilder urlBuilder;
	@Mock ContentRetrievalService contentRetrievalService;
	@Mock CommonModelObjectsService commonModelObjectsService;
	
	@Mock User existingUser;	
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private List<User> allActiveUsers;

	private ProfileController controller;
	@Mock List<Resource> existingUsersSubmittedItems;
	@Mock List<FrontendResource> existingUsersTaggedItems;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		controller = new ProfileController(userDao, loggedInUserFilter, urlBuilder, contentRetrievalService, commonModelObjectsService);
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}
	
	@Test
	public void allActiveProfilesShouldBeShownOnProfilesIndex() throws Exception {
		when(userDao.getActiveUsers()).thenReturn(allActiveUsers);
		ModelAndView mv = controller.all(request, response);
		assertEquals(allActiveUsers, mv.getModel().get("profiles"));
	}
	
	@Test
	public void usersPostsAndTaggingHistoryShouldBeFetchedFromTheContentRetrievalService() throws Exception {
		request.setPathInfo("/profiles/" + VALID_PROFILE_NAME);
		when(userDao.getUserByProfileName(VALID_PROFILE_NAME)).thenReturn(existingUser);
		when(contentRetrievalService.getOwnedBy(existingUser)).thenReturn(existingUsersSubmittedItems);
		when(contentRetrievalService.getTaggedBy(existingUser)).thenReturn(existingUsersTaggedItems);

		ModelAndView mv = controller.view(request, response);
		
		assertEquals(existingUsersSubmittedItems, mv.getModel().get("submitted"));
		assertEquals(existingUsersTaggedItems, mv.getModel().get("tagged"));
	}
	
	@Test
	public void lettersAndNumbersIsValidNewProfileName() throws Exception {
		when(userDao.getUserByProfileName(VALID_PROFILE_NAME)).thenReturn(null);
		assertTrue(controller.isValidNewProfilename(VALID_PROFILE_NAME));
	}
	
	@Test
	public void nonLettersAndNumbersAreNotValidInProfileNames() throws Exception {
		when(userDao.getUserByProfileName(INVALID_PROFILE_NAME)).thenReturn(null);
		assertFalse(controller.isValidNewProfilename(INVALID_PROFILE_NAME));
	}
	
	@Test
	public void newProfileNamesMustNotAlreadyBeTaken() throws Exception {
		when(userDao.getUserByProfileName(VALID_PROFILE_NAME)).thenReturn(existingUser);
		assertFalse(controller.isValidNewProfilename(VALID_PROFILE_NAME));
	}
			
}
