package nz.co.searchwellington.controllers.profiles

import nz.co.searchwellington.controllers.{CommonModelObjectsService, LoggedInUserFilter}
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.{ContentRetrievalService, HibernateBackedUserDAO}
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.{MockitoAnnotations, when}
import org.springframework.mock.web.{MockHttpServletRequest, MockHttpServletResponse}
import org.springframework.web.servlet.ModelAndView

object ProfileControllerTest {
  private val VALID_PROFILE_NAME: String = "tonytw1"
  private val INVALID_PROFILE_NAME: String = "tony-tw1"
}

class ProfileControllerTest {
  @Mock private[profiles] val userDao: HibernateBackedUserDAO = null
  @Mock private[profiles] val loggedInUserFilter: LoggedInUserFilter = null
  @Mock private[profiles] val urlBuilder: UrlBuilder = null
  @Mock private[profiles] val contentRetrievalService: ContentRetrievalService = null
  @Mock private[profiles] val commonModelObjectsService: CommonModelObjectsService = null
  @Mock private[profiles] val existingUser: User = null
  private var request: MockHttpServletRequest = null
  private var response: MockHttpServletResponse = null
  private val allActiveUsers: Seq[User] = null
  private var controller: ProfileController = null
  @Mock private[profiles] val existingUsersSubmittedItems: Seq[FrontendResource] = null
  @Mock private[profiles] val existingUsersTaggedItems: Seq[FrontendResource] = null

  @Before def setup {
    MockitoAnnotations.initMocks(this)
    controller = new ProfileController(userDao, loggedInUserFilter, urlBuilder, contentRetrievalService, commonModelObjectsService)
    request = new MockHttpServletRequest
    response = new MockHttpServletResponse
  }

  @Test
  @throws[Exception]
  def allActiveProfilesShouldBeShownOnProfilesIndex {
    when(userDao.getActiveUsers).thenReturn(allActiveUsers)
    val mv: ModelAndView = controller.all(request, response)
    assertEquals(allActiveUsers, mv.getModel.get("profiles"))
  }

  @Test
  @throws[Exception]
  def usersPostsAndTaggingHistoryShouldBeFetchedFromTheContentRetrievalService {
    request.setPathInfo("/profiles/" + ProfileControllerTest.VALID_PROFILE_NAME)
    when(userDao.getUserByProfileName(ProfileControllerTest.VALID_PROFILE_NAME)).thenReturn(existingUser)
    when(contentRetrievalService.getOwnedBy(existingUser)).thenReturn(existingUsersSubmittedItems)
    when(contentRetrievalService.getTaggedBy(existingUser)).thenReturn(existingUsersTaggedItems)
    val mv: ModelAndView = controller.view(request, response)
    assertEquals(existingUsersSubmittedItems, mv.getModel.get("submitted"))
    assertEquals(existingUsersTaggedItems, mv.getModel.get("tagged"))
  }

  @Test
  @throws[Exception]
  def lettersAndNumbersIsValidNewProfileName {
    when(userDao.getUserByProfileName(ProfileControllerTest.VALID_PROFILE_NAME)).thenReturn(null)
    assertTrue(controller.isValidNewProfilename(ProfileControllerTest.VALID_PROFILE_NAME))
  }

  @Test
  @throws[Exception]
  def nonLettersAndNumbersAreNotValidInProfileNames {
    when(userDao.getUserByProfileName(ProfileControllerTest.INVALID_PROFILE_NAME)).thenReturn(null)
    assertFalse(controller.isValidNewProfilename(ProfileControllerTest.INVALID_PROFILE_NAME))
  }

  @Test
  @throws[Exception]
  def newProfileNamesMustNotAlreadyBeTaken {
    when(userDao.getUserByProfileName(ProfileControllerTest.VALID_PROFILE_NAME)).thenReturn(existingUser)
    assertFalse(controller.isValidNewProfilename(ProfileControllerTest.VALID_PROFILE_NAME))
  }

}