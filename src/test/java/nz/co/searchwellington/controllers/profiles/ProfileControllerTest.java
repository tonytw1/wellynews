package nz.co.searchwellington.controllers.profiles;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

public class ProfileControllerTest {

	private static final String VALID_PROFILE_NAME = "tonytw1";
	@Mock UserRepository userDao;
	@Mock LoggedInUserFilter loggedInUserFilter;
	@Mock UrlBuilder urlBuilder;
	@Mock TagDAO tagDAO;
	@Mock ContentRetrievalService contentRetrievalService;
	@Mock User existingUser;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void lettersAndNumbersIsValidNewProfileName() throws Exception {
		Mockito.when(userDao.getUserByProfileName(VALID_PROFILE_NAME)).thenReturn(null);
		ProfileController controller = new ProfileController(userDao, loggedInUserFilter, urlBuilder, tagDAO, contentRetrievalService);
		assertTrue(controller.isValidNewProfilename(VALID_PROFILE_NAME));
	}
	
	@Test
	public void newProfileNamesMustNotAlreadyBeTaken() throws Exception {
		Mockito.when(userDao.getUserByProfileName(VALID_PROFILE_NAME)).thenReturn(existingUser);
		ProfileController controller = new ProfileController(userDao, loggedInUserFilter, urlBuilder, tagDAO, contentRetrievalService);
		assertFalse(controller.isValidNewProfilename(VALID_PROFILE_NAME));
	}
			
}
