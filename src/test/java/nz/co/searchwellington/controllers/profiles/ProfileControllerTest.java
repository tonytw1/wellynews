package nz.co.searchwellington.controllers.profiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.model.User;
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
	private HttpServletRequest request;
	private HttpServletResponse response;
	private List<User> allActiveUsers;

	private ProfileController controller;
	
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
	public void lettersAndNumbersIsValidNewProfileName() throws Exception {
		Mockito.when(userDao.getUserByProfileName(VALID_PROFILE_NAME)).thenReturn(null);
		assertTrue(controller.isValidNewProfilename(VALID_PROFILE_NAME));
	}
	
	@Test
	public void newProfileNamesMustNotAlreadyBeTaken() throws Exception {
		Mockito.when(userDao.getUserByProfileName(VALID_PROFILE_NAME)).thenReturn(existingUser);
		ProfileController controller = new ProfileController(userDao, loggedInUserFilter, urlBuilder, tagDAO, contentRetrievalService);
		assertFalse(controller.isValidNewProfilename(VALID_PROFILE_NAME));
	}
			
}
