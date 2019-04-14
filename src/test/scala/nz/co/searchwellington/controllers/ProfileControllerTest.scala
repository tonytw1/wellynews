package nz.co.searchwellington.controllers

import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertFalse, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import org.mockito.{Mock, MockitoAnnotations}
import org.springframework.mock.web.{MockHttpServletRequest, MockHttpServletResponse}
import org.junit.Assert.assertEquals

import scala.concurrent.Future

class ProfileControllerTest {

  private val VALID_PROFILE_NAME: String = "tonytw1"
  private val INVALID_PROFILE_NAME: String = "tony-tw1"

  private val mongoRepository = mock(classOf[MongoRepository])
  @Mock private val loggedInUserFilter: LoggedInUserFilter = null
  @Mock private val urlBuilder: UrlBuilder = null
  @Mock private val contentRetrievalService: ContentRetrievalService = null
  @Mock private val commonModelObjectsService: CommonModelObjectsService = null
  @Mock private val existingUser: User = null
  private var request: MockHttpServletRequest = null
  private var response: MockHttpServletResponse = null

  private val aUser = mock(classOf[User])
  private val anotherUser = mock(classOf[User])
  private val allActiveUsers = Seq(aUser, anotherUser)

  private var controller: ProfileController = null
  @Mock private val existingUsersSubmittedItems: Seq[FrontendResource] = null
  @Mock private val existingUsersTaggedItems: Seq[FrontendResource] = null

  @Before def setup {
    MockitoAnnotations.initMocks(this)
    controller = new ProfileController(mongoRepository, loggedInUserFilter, urlBuilder, contentRetrievalService, commonModelObjectsService)
    request = new MockHttpServletRequest
    response = new MockHttpServletResponse
  }

  @Test
  def allActiveProfilesShouldBeShownOnProfilesIndex {
    when(mongoRepository.getAllUsers()).thenReturn(Future.successful(allActiveUsers))
    val mv = controller.profiles(request, response)
    assertEquals(allActiveUsers, mv.getModel.get("profiles"))
  }

  /*
  @Test
  @throws[Exception]
  def usersPostsAndTaggingHistoryShouldBeFetchedFromTheContentRetrievalService {
    request.setPathInfo("/profiles/" + VALID_PROFILE_NAME)
    when(mongoRepository.getUserByProfilename(VALID_PROFILE_NAME)).thenReturn(Future.successful(Some(existingUser)))
    when(contentRetrievalService.getOwnedBy(existingUser)).thenReturn(existingUsersSubmittedItems)
    when(contentRetrievalService.getTaggedBy(existingUser)).thenReturn(existingUsersTaggedItems)
    val mv = controller.view(request, response)
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
  def newProfileNamesMustBeValidNotAlreadyTaken {
    when(mongoRepository.getUserByProfilename(VALID_PROFILE_NAME)).thenReturn(Future.successful(Some(existingUser)))
    assertFalse(controller.isValidAvailableProfilename(VALID_PROFILE_NAME))
  }

}