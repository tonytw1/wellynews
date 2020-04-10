package nz.co.searchwellington.controllers.models.helpers

import java.util.regex.Pattern

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Website
import nz.co.searchwellington.model.frontend.{FrontendResource, FrontendWebsite}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.joda.time.{DateTime, Interval}
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.Test
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class PublisherMonthModelBuilderTest extends ReasonableWaits with ContentFields {

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val frontendResourceMapper = mock(classOf[FrontendResourceMapper])

  private val publisher = Website(title = Some("A publisher"), url_words = Some("a-publisher"))

  private val newsitem = mock(classOf[FrontendResource])
  private val anotherNewsitem = mock(classOf[FrontendResource])
  private val monthNewsitems = Seq(newsitem, anotherNewsitem)

  private val modelBuilder = new PublisherMonthModelBuilder(contentRetrievalService, frontendResourceMapper)

  @Test
  def testPathMatcher(): Unit = {
    val regex = "/.*/[0-9]+-.*?"
    val pattern = Pattern.compile(regex)

    assertTrue(pattern.matcher("/wcc/2019-feb").matches())
    assertFalse(pattern.matcher("/wcc/rss").matches())
  }

  @Test
  def isValidForPublisherAndMonthPath(): Unit = {
    val request = new MockHttpServletRequest()
    request.setAttribute("publisher", publisher)
    request.setPathInfo("/a-publisher/2020-feb")

    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def isNotValidForPublisherAndMonthPath(): Unit = {
    val archiveMonthRequest = new MockHttpServletRequest
    archiveMonthRequest.setPathInfo("/2020-may")

    assertFalse(modelBuilder.isValid(archiveMonthRequest))
  }

  @Test
  def isValidForPublisherNonDate(): Unit = {
    val request = new MockHttpServletRequest()
    request.setAttribute("publisher", publisher)
    request.setPathInfo("/a-publisher/something")

    assertFalse(modelBuilder.isValid(request))
  }

  @Test
  def mainContentIsPublishersNewsitemsForMonth(): Unit = {
    val request = new MockHttpServletRequest
    request.setAttribute("publisher", publisher)
    request.setPathInfo("/a-publisher/2020-jul")

    val july = new DateTime(2020, 7, 1, 0, 0)
    when(contentRetrievalService.getNewsitemsForPublisherInterval(publisher, new Interval(july, july.plusMonths(1)), None)).thenReturn(Future.successful(monthNewsitems))
    when(frontendResourceMapper.createFrontendResourceFrom(publisher)).thenReturn(Future.successful(FrontendWebsite(id = "123")))

    val mv = Await.result(modelBuilder.populateContentModel(request, null), TenSeconds).get

    import scala.collection.JavaConverters._
    assertEquals(monthNewsitems.asJava, mv.getModel.get(MAIN_CONTENT))
  }

}
