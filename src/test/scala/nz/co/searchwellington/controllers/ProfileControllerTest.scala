package nz.co.searchwellington.controllers

import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.{MockHttpServletRequest, MockHttpServletResponse}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ProfileControllerTest {

  private val VALID_PROFILE_NAME = "tonytw1"
  private val INVALID_PROFILE_NAME = "tony-tw1"

  private val mongoRepository = mock(classOf[MongoRepository])
  private val loggedInUserFilter = mock(classOf[LoggedInUserFilter])
  private val urlBuilder = mock(classOf[UrlBuilder])
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val existingUser: User = mock(classOf[User])

  private val aUser = mock(classOf[User])
  private val anotherUser = mock(classOf[User])
  private val allActiveUsers = Seq(aUser, anotherUser)

  private val aResource = mock(classOf[FrontendResource])
  private val anotherResource = mock(classOf[FrontendResource])
  private val existingUsersSubmittedItems = Seq(aResource, anotherResource)
  private val existingUsersTaggedItems = Seq(anotherResource)

  private val controller = new ProfileController(mongoRepository, loggedInUserFilter, urlBuilder, contentRetrievalService)

  private var request: MockHttpServletRequest = null
  private var response: MockHttpServletResponse = null

  private val loggedInUser = None

  @Before def setup {
    request = new MockHttpServletRequest
    response = new MockHttpServletResponse
  }

  @Test
  def allActiveProfilesShouldBeShownOnProfilesIndex {
    when(mongoRepository.getAllUsers).thenReturn(Future.successful(allActiveUsers))
    when(contentRetrievalService.getTopLevelTags).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getFeaturedTags).thenReturn(Future.successful(Seq.empty))

    val mv = controller.profiles(request, response)

    assertEquals(allActiveUsers, mv.getModel.get("profiles"))
  }

  @Test
  def usersPostsAndTaggingHistoryShouldBeFetchedFromTheContentRetrievalService {
    request.setPathInfo("/profiles/" + VALID_PROFILE_NAME)
    when(mongoRepository.getUserByProfilename(VALID_PROFILE_NAME)).thenReturn(Future.successful(Some(existingUser)))
    when(contentRetrievalService.getOwnedBy(existingUser, loggedInUser)).thenReturn(existingUsersSubmittedItems)
    when(contentRetrievalService.getTaggedBy(existingUser, loggedInUser)).thenReturn(existingUsersTaggedItems)
    when(contentRetrievalService.getTopLevelTags).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getFeaturedTags).thenReturn(Future.successful(Seq.empty))

    val mv = controller.profile(request, response)

    import scala.collection.JavaConverters._
    assertEquals(existingUsersSubmittedItems.asJava, mv.getModel.get("submitted"))
    assertEquals(existingUsersTaggedItems.asJava, mv.getModel.get("tagged"))
  }

  @Test
  def lettersAndNumbersIsValidNewProfileName {
    when(mongoRepository.getUserByProfilename(VALID_PROFILE_NAME)).thenReturn(Future.successful(None))
    assertTrue(controller.isValidAvailableProfilename(VALID_PROFILE_NAME))
  }

  @Test
  def nonLettersAndNumbersAreNotValidInProfileNames {
    when(mongoRepository.getUserByProfilename(INVALID_PROFILE_NAME)).thenReturn(Future.successful(None))
    assertFalse(controller.isValidAvailableProfilename(INVALID_PROFILE_NAME))
  }

  @Test
  def newProfileNamesMustBeValidNotAlreadyTaken {
    when(mongoRepository.getUserByProfilename(VALID_PROFILE_NAME)).thenReturn(Future.successful(Some(existingUser)))
    assertFalse(controller.isValidAvailableProfilename(VALID_PROFILE_NAME))
  }

}