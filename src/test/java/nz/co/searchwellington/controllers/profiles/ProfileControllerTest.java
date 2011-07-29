package nz.co.searchwellington.controllers.profiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.repositories.TagDAO;
import nz.co.searchwellington.repositories.UserRepository;
import nz.co.searchwellington.urls.UrlBuilder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

public class ProfileControllerTest {

	private static final String VALID_PROFILE_NAME = "tonytw1";
	@Mock UserRepository userDao;
	@Mock LoggedInUserFilter loggedInUserFilter;
	@Mock UrlBuilder urlBuilder;
	@Mock TagDAO tagDAO;
	@Mock ContentRetrievalService contentRetrievalService;
	
	@Mock User existingUser;	
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private List<User> allActiveUsers;

	private ProfileController controller;
	@Mock List<FrontendResource> existingUsersSubmittedItems;
	@Mock List<FrontendResource> existingUsersTaggedItems;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		controller = new ProfileController(userDao, loggedInUserFilter, urlBuilder, tagDAO, contentRetrievalService);
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}
	
	@Test
	public void allActiveProfilesShouldBeShownOnProfilesIndex() throws Exception {
		Mockito.when(userDao.getActiveUsers()).thenReturn(allActiveUsers);
		ModelAndView mv = controller.all(request, response);
		assertEquals(allActiveUsers, mv.getModel().get("profiles"));
	}
	
	@Test
	public void usersPostsAndTaggingHistoryShouldBeFetchedFromTheContentRetrievalService() throws Exception {
		request.setPathInfo("/profiles/" + VALID_PROFILE_NAME);
		Mockito.when(userDao.getUserByProfileName(VALID_PROFILE_NAME)).thenReturn(existingUser);
		Mockito.when(contentRetrievalService.getOwnedBy(existingUser, 30)).thenReturn(existingUsersSubmittedItems);
		Mockito.when(contentRetrievalService.getTaggedBy(existingUser, 30)).thenReturn(existingUsersTaggedItems);

		ModelAndView mv = controller.view(request, response);
		
		assertEquals(existingUsersSubmittedItems, mv.getModel().get("submitted"));
		assertEquals(existingUsersTaggedItems, mv.getModel().get("tagged"));
	}
	
	@Test
	public void lettersAndNumbersIsValidNewProfileName() throws Exception {
		Mockito.when(userDao.getUserByProfileName(VALID_PROFILE_NAME)).thenReturn(null);
		assertTrue(controller.isValidNewProfilename(VALID_PROFILE_NAME));
	}
	
	@Test
	public void newProfileNamesMustNotAlreadyBeTaken() throws Exception {
		Mockito.when(userDao.getUserByProfileName(VALID_PROFILE_NAME)).thenReturn(existingUser);
		assertFalse(controller.isValidNewProfilename(VALID_PROFILE_NAME));
	}
			
}
