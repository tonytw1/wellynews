package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.Test
import org.mockito.Mockito.{mock, when}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

class ProfileControllerTest extends ReasonableWaits {

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

  private val controller = new ProfileController(mongoRepository, loggedInUserFilter, urlBuilder, contentRetrievalService)

  @Test
  def allActiveProfilesShouldBeShownOnProfilesIndex(): Unit = {
    when(mongoRepository.getAllUsers).thenReturn(Future.successful(allActiveUsers))
    when(contentRetrievalService.getTopLevelTags).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getFeaturedTags).thenReturn(Future.successful(Seq.empty))
    when(loggedInUserFilter.getLoggedInUser).thenReturn(None)
    when(contentRetrievalService.getLatestNewsitems(maxItems = 5, loggedInUser = None)).thenReturn(Future.successful(Seq.empty))

    val mv = controller.profiles

    assertEquals(allActiveUsers.asJava, mv.getModel.get("profiles"))
  }

  @Test
  def lettersAndNumbersIsValidNewProfileName(): Unit = {
    when(mongoRepository.getUserByProfilename(VALID_PROFILE_NAME)).thenReturn(Future.successful(None))
    assertTrue(Await.result(controller.isValidAvailableProfilename(VALID_PROFILE_NAME), TenSeconds))
  }

  @Test
  def nonLettersAndNumbersAreNotValidInProfileNames(): Unit = {
    when(mongoRepository.getUserByProfilename(INVALID_PROFILE_NAME)).thenReturn(Future.successful(None))
    assertFalse(Await.result(controller.isValidAvailableProfilename(INVALID_PROFILE_NAME), TenSeconds))
  }

  @Test
  def newProfileNamesMustBeValidNotAlreadyTaken(): Unit = {
    when(mongoRepository.getUserByProfilename(VALID_PROFILE_NAME)).thenReturn(Future.successful(Some(existingUser)))
    assertFalse(Await.result(controller.isValidAvailableProfilename(VALID_PROFILE_NAME), TenSeconds))
  }

}