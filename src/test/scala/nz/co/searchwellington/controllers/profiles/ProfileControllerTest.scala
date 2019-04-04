package nz.co.searchwellington.controllers.profiles

import nz.co.searchwellington.controllers.{CommonModelObjectsService, LoggedInUserFilter}
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import org.mockito.{Mock, MockitoAnnotations}
import org.springframework.mock.web.{MockHttpServletRequest, MockHttpServletResponse}
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.Future

class ProfileControllerTest {

  private val VALID_PROFILE_NAME: String = "tonytw1"
  private val INVALID_PROFILE_NAME: String = "tony-tw1"

  private val mongoRepository = mock(classOf[MongoRepository])
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
    controller = new ProfileController(mongoRepository, loggedInUserFilter, urlBuilder, contentRetrievalService, commonModelObjectsService)
    request = new MockHttpServletRequest
    response = new MockHttpServletResponse
  }

  /*
  @Test
  @throws[Exception]
  def allActiveProfilesShouldBeShownOnProfilesIndex {
    when(mongoRepository.getAllUsers()).thenReturn(Future.successful(allActiveUsers))
    val mv = controller.all(request, response)
    assertEquals(allActiveUsers, mv.getModel.get("profiles"))
  }

  @Test
  @throws[Exception]
  def usersPostsAndTaggingHistoryShouldBeFetchedFromTheContentRetrievalService {
    request.setPathInfo("/profiles/" + VALID_PROFILE_NAME)
    when(mongoRepository.getUserByProfilename(VALID_PROFILE_NAME)).thenReturn(Future.successful(Some(existingUser)))
    when(contentRetrievalService.getOwnedBy(existingUser)).thenReturn(existingUsersSubmittedItems)
    when(contentRetrievalService.getTaggedBy(existingUser)).thenReturn(existingUsersTaggedItems)
    val mv: ModelAndView = controller.view(request, response)
    assertEquals(existingUsersSubmittedItems, mv.getModel.get("submitted"))
    assertEquals(existingUsersTaggedItems, mv.getModel.get("tagged"))
  }
  */

  @Test
  @throws[Exception]
  def lettersAndNumbersIsValidNewProfileName {
    when(mongoRepository.getUserByProfilename(VALID_PROFILE_NAME)).thenReturn(Future.successful(None))
    assertTrue(controller.isValidAvailableProfilename(VALID_PROFILE_NAME))
  }

  @Test
  @throws[Exception]
  def nonLettersAndNumbersAreNotValidInProfileNames {
    when(mongoRepository.getUserByProfilename(INVALID_PROFILE_NAME)).thenReturn(Future.successful(None))
    assertFalse(controller.isValidAvailableProfilename(INVALID_PROFILE_NAME))
  }

  @Test
  @throws[Exception]
  def newProfileNamesMustNotAlreadyBeTaken {
    when(mongoRepository.getUserByProfilename(VALID_PROFILE_NAME)).thenReturn(Future.successful(Some(existingUser)))
    assertFalse(controller.isValidAvailableProfilename(VALID_PROFILE_NAME))
  }

}