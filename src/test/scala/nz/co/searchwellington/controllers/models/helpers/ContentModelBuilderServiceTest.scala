package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.ContentModelBuilderService
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.junit.Assert.assertEquals
import org.junit.{Before, Test}
import org.mockito.Matchers.anyString
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory
import uk.co.eelpieconsulting.common.views.json.JsonView
import uk.co.eelpieconsulting.common.views.rss.RssView

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class ContentModelBuilderServiceTest extends ReasonableWaits {

  private val viewFactory = mock(classOf[ViewFactory])
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])

  private val request = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/something")
    request
  }

  private val validModelAndView = new ModelAndView("")

  private val invalidModelBuilder = mock(classOf[ModelBuilder])
  private val validModelBuilder =  mock(classOf[ModelBuilder])

  @Test
  def shouldDelegateModelBuildingToTheFirstBuildWhoSaysTheyAreValid(): Unit = {
    when(invalidModelBuilder.isValid(request)).thenReturn(false)
    when(validModelBuilder.isValid(request)).thenReturn(true)
    when(validModelBuilder.populateContentModel(request, None)).thenReturn(Future.successful(Some(validModelAndView)))
    when(validModelBuilder.populateExtraModelContent(request, validModelAndView, None)).thenReturn(Future.successful(validModelAndView))

    when(contentRetrievalService.getTopLevelTags).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getFeaturedTags).thenReturn(Future.successful(Seq.empty))

    val contentModelBuilderService = new ContentModelBuilderService(viewFactory,
      contentRetrievalService,
      Seq(invalidModelBuilder, validModelBuilder)
    )

    val result = Await.result(contentModelBuilderService.populateContentModel(request), TenSeconds)

    assertEquals(Some(validModelAndView), result)
  }

  @Test
  def shouldReturnNullIfNoModelBuilderWasFoundForRequest(): Unit = {
    when(invalidModelBuilder.isValid(request)).thenReturn(false)
    val contentModelBuilderService = new ContentModelBuilderService(viewFactory, contentRetrievalService, Seq(invalidModelBuilder))

    val result = Await.result(contentModelBuilderService.populateContentModel(request), TenSeconds)

    assertEquals(None, result)
  }

  @Test
  def rssSuffixedRequestsShouldBeGivenTheRssView(): Unit = {
    when(invalidModelBuilder.isValid(request)).thenReturn(false)
    when(validModelBuilder.isValid(request)).thenReturn(true)
    when(validModelBuilder.populateContentModel(request, None)).thenReturn(Future.successful(Some(validModelAndView)))

    val contentModelBuilderService = new ContentModelBuilderService(
      viewFactory,
      contentRetrievalService,
      Seq(invalidModelBuilder, validModelBuilder)
    )

    val rssView = mock(classOf[RssView])
    when(viewFactory.getRssView(anyString, anyString, anyString)).thenReturn(rssView)
    request.setRequestURI("/something/rss")
    assertEquals(rssView, Await.result(contentModelBuilderService.populateContentModel(request), TenSeconds).get.getView)
  }

  @Test
  def jsonSuffixedRequestsShouldBeGivenTheRssView(): Unit = {
    when(invalidModelBuilder.isValid(request)).thenReturn(false)
    when(validModelBuilder.isValid(request)).thenReturn(true)
    when(validModelBuilder.populateContentModel(request, None)).thenReturn(Future.successful(Some(validModelAndView)))

    val contentModelBuilderService = new ContentModelBuilderService(
      viewFactory,
      contentRetrievalService,
      Seq(invalidModelBuilder, validModelBuilder)
    )

    val jsonView = mock(classOf[JsonView])
    when(viewFactory.getJsonView).thenReturn(jsonView)
    request.setRequestURI("/something/json")
    assertEquals(jsonView, Await.result(contentModelBuilderService.populateContentModel(request), TenSeconds).get.getView)
  }

}
