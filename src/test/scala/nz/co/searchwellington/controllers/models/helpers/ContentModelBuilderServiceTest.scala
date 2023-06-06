package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.ContentModelBuilderService
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.{MockHttpServletRequest, MockHttpServletResponse}
import org.springframework.ui.ModelMap
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.json.JsonView
import uk.co.eelpieconsulting.common.views.rss.RssView
import uk.co.eelpieconsulting.common.views.{EtagGenerator, ViewFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class ContentModelBuilderServiceTest extends ReasonableWaits {

  private val viewFactory = new ViewFactory(new EtagGenerator)
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])

  private implicit val currentSpan: Span = Span.current()

  private val request = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/something")
    request
  }

  private val response = new MockHttpServletResponse

  private val validModel = new ModelMap("").addAttribute("something", "abc")
  private val validExtras = new ModelMap().addAttribute("somethingextra", "xyz")

  private val invalidModelBuilder = mock(classOf[ModelBuilder])
  private val validModelBuilder =  mock(classOf[ModelBuilder])

  private val contentModelBuilderService = new ContentModelBuilderService(viewFactory,
    contentRetrievalService,
    Seq(invalidModelBuilder, validModelBuilder)
  )

  @Test
  def shouldDelegateModelBuildingToTheFirstBuildWhoSaysTheyAreValid(): Unit = {
    when(invalidModelBuilder.isValid(request)).thenReturn(false)
    when(validModelBuilder.isValid(request)).thenReturn(true)
    when(validModelBuilder.populateContentModel(request, None)).thenReturn(Future.successful(Some(validModel)))
    when(validModelBuilder.populateExtraModelContent(request, None)).thenReturn(Future.successful(validExtras))
    when(validModelBuilder.getViewName(validModel, None)).thenReturn("a-view")

    when(contentRetrievalService.getTopLevelTags).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getFeaturedTags).thenReturn(Future.successful(Seq.empty))

    val result = Await.result(contentModelBuilderService.buildModelAndView(request, response), TenSeconds)

    val expectedModelAndView = new ModelAndView("a-view").addAllObjects(validModel).addAllObjects(validExtras)
    assertEquals(expectedModelAndView.getModel, result.get.getModel)
    assertEquals("a-view", result.get.getViewName)
  }

  @Test
  def shouldMergeExtrasOntoTheModelAndViewForHtmlPages(): Unit = {
    when(validModelBuilder.isValid(request)).thenReturn(true)
    when(validModelBuilder.populateContentModel(request, None)).thenReturn(Future.successful(Some(validModel)))
    when(validModelBuilder.populateExtraModelContent(request, None)).thenReturn(Future.successful(validExtras))

    when(contentRetrievalService.getTopLevelTags).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getFeaturedTags).thenReturn(Future.successful(Seq.empty))

    val result = Await.result(contentModelBuilderService.buildModelAndView(request, response), TenSeconds)

    val model = result.get.getModel
    assertEquals("abc", model.get("something"))
    assertEquals("xyz", model.get("somethingextra"))
  }

  @Test
  def shouldSetLoggedInUser(): Unit = {
    val loggedInUser = User(name = Some("A user"))
    when(invalidModelBuilder.isValid(request)).thenReturn(false)
    when(validModelBuilder.isValid(request)).thenReturn(true)
    when(validModelBuilder.populateContentModel(request, Some(loggedInUser))).thenReturn(Future.successful(Some(validModel)))
    when(validModelBuilder.populateExtraModelContent(request, Some(loggedInUser))).thenReturn(Future.successful(validExtras))

    when(contentRetrievalService.getTopLevelTags).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getFeaturedTags).thenReturn(Future.successful(Seq.empty))

    val result = Await.result(contentModelBuilderService.buildModelAndView(request, response, Some(loggedInUser)), TenSeconds)

    assertEquals(loggedInUser, result.get.getModel.get("loggedInUser"))
  }

  @Test
  def shouldReturnNoneIfNoValidModelBuilderWasFoundForRequest(): Unit = {
    when(validModelBuilder.isValid(request)).thenReturn(false)
    when(invalidModelBuilder.isValid(request)).thenReturn(false)

    val result = Await.result(contentModelBuilderService.buildModelAndView(request, response), TenSeconds)

    assertTrue(result.isEmpty)
  }

  @Test
  def rssSuffixedRequestsShouldBeGivenTheRssView(): Unit = {
    when(invalidModelBuilder.isValid(request)).thenReturn(false)
    when(validModelBuilder.isValid(request)).thenReturn(true)
    when(validModelBuilder.populateContentModel(request, None)).thenReturn(Future.successful(Some(validModel)))

    request.setRequestURI("/something/rss")

    val view = Await.result(contentModelBuilderService.buildModelAndView(request, response), TenSeconds).get.getView

    assertTrue(view.isInstanceOf[RssView])
  }

  @Test
  def jsonSuffixedRequestsShouldBeGivenTheRssView(): Unit = {
    when(invalidModelBuilder.isValid(request)).thenReturn(false)
    when(validModelBuilder.isValid(request)).thenReturn(true)
    when(validModelBuilder.populateContentModel(request, None)).thenReturn(Future.successful(Some(validModel)))
    request.setRequestURI("/something/json")

    val view = Await.result(contentModelBuilderService.buildModelAndView(request, response), TenSeconds).get.getView

    assertTrue(view.isInstanceOf[JsonView])
  }

}
