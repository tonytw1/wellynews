package nz.co.searchwellington.controllers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.controllers.models.{ContentModelBuilderService, ContentModelBuilderServiceFactory}
import nz.co.searchwellington.filters.RequestFilter
import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse, assertTrue, fail}
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, verify, verifyNoInteractions, when}
import org.springframework.http.HttpStatus
import org.springframework.util.AntPathMatcher
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._
import scala.util.Try

class ContentControllerTest {
  private val contentModelBuilderServiceFactory = mock(classOf[ContentModelBuilderServiceFactory])
  private val contentModelBuilderService = mock(classOf[ContentModelBuilderService])
  private val urlStack = mock(classOf[UrlStack])
  private val requestFilter = mock(classOf[RequestFilter])
  private val loggedInUserFilter = mock(classOf[LoggedInUserFilter])

  private val request = mock(classOf[HttpServletRequest])
  private val unknownPathRequest = mock(classOf[HttpServletRequest])
  private val response = mock(classOf[HttpServletResponse])

  private implicit val currentSpan: Span = Span.current()

  private def contentController = new ContentController(contentModelBuilderServiceFactory, urlStack, requestFilter, loggedInUserFilter)

  @Test
  def shouldDelegateToTheContentModelBuilderToGetTheModelForThisRequest(): Unit = {
    val expectedModelAndView = new ModelAndView("a-view", Map("foo" -> "bar").asJava)
    when(contentModelBuilderServiceFactory.makeContentModelBuilderService()).thenReturn(contentModelBuilderService)
    when(contentModelBuilderService.buildModelAndView(request)).thenReturn(Future.successful(Some(expectedModelAndView)))

    val modelAndView = contentController.normal(request, response)

    assertEquals(expectedModelAndView, modelAndView)
  }

  @Test
  def should404IfNoModelWasAvailableForThisRequest(): Unit = {
    when(contentModelBuilderServiceFactory.makeContentModelBuilderService()).thenReturn(contentModelBuilderService)
    when(contentModelBuilderService.buildModelAndView(unknownPathRequest)).thenReturn(Future.successful(None))

    try {
      contentController.normal(unknownPathRequest, response)
      fail()
    } catch {
      case r: ResponseStatusException =>
        assertEquals(HttpStatus.NOT_FOUND, r.getStatus)
      case _: Throwable =>
        fail()
    }
  }

  @Test
  def shouldNotPush404sOntoTheReturnToUrlStack(): Unit = {
    when(contentModelBuilderServiceFactory.makeContentModelBuilderService()).thenReturn(contentModelBuilderService)
    when(contentModelBuilderService.buildModelAndView(unknownPathRequest)).thenReturn(Future.successful(None))

    val triedView = Try {
      contentController.normal(unknownPathRequest, response)
    }

    assertTrue(triedView.isFailure)
    verifyNoInteractions(urlStack)
  }

  @Test
  def htmlPageViewsShouldBePutOntoTheUrlStack(): Unit = {
    val expectedModelAndView = new ModelAndView("a-view", Map("foo" -> "bar").asJava)
    when(contentModelBuilderServiceFactory.makeContentModelBuilderService()).thenReturn(contentModelBuilderService)
    when(contentModelBuilderService.buildModelAndView(request)).thenReturn(Future.successful(Some(expectedModelAndView)))

    contentController.normal(request, response)

    verify(urlStack).setUrlStack(request)
  }

  @Test
  def shouldMatchContentPaths(): Unit = {
    // Practise our AntPathMatcher expressions here
    assertTrue(new AntPathMatcher().`match`("/*", "/transport"))
    assertTrue(new AntPathMatcher().`match`("/*/rss", "/transport/rss"))

    val publisherArchivePattern = "/{\\w+}/{year:\\d+}-{month:\\w+}"
    val publisherArchivePath = "/wellington-city-council/2021-nov"
    assertTrue(new AntPathMatcher().`match`(publisherArchivePattern, publisherArchivePath))

    assertFalse(new AntPathMatcher().`match`(publisherArchivePattern, "/static/palm.jpg"))
    assertFalse(new AntPathMatcher().`match`(publisherArchivePattern, "/static/feed-icon-16x16.png"))
  }

}