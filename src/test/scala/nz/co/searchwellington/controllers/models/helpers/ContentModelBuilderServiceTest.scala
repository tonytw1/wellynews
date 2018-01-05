package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.controllers.CommonModelObjectsService
import nz.co.searchwellington.controllers.models.ContentModelBuilderService
import nz.co.searchwellington.controllers.models.JsonCallbackNameValidator
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.model.Tag
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory
import uk.co.eelpieconsulting.common.views.json.JsonView
import uk.co.eelpieconsulting.common.views.rss.RssView
import java.util.List
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.mockito.Mockito.when

class ContentModelBuilderServiceTest {

  private val CALLBACK: String = "callback"

  @Mock private[helpers] val viewFactory: ViewFactory = null
  @Mock private[helpers] val jsonCallbackNameValidator: JsonCallbackNameValidator = null
  @Mock private[helpers] val commonModelObjectsService: CommonModelObjectsService = null
  private[helpers] var request: MockHttpServletRequest = null
  @Mock private[helpers] val jsonView: JsonView = null
  @Mock private[helpers] val rssView: RssView = null
  @Mock private[helpers] val featuredTags: List[Tag] = null
  @Mock private[helpers] val topLevelTags: List[Tag] = null
  private[helpers] var validModelAndView: ModelAndView = null
  @Mock private[helpers] val invalidModelBuilder: ModelBuilder = null
  @Mock private[helpers] val validModelBuilder: ModelBuilder = null
  private[helpers] var contentModelBuilderService: ContentModelBuilderService = null

  @Before def setup {
    MockitoAnnotations.initMocks(this)
    request = new MockHttpServletRequest
    validModelAndView = new ModelAndView
    request.setPathInfo("/something")
    when(invalidModelBuilder.isValid(request)).thenReturn(false)
    when(validModelBuilder.isValid(request)).thenReturn(true)
    when(validModelBuilder.populateContentModel(request)).thenReturn(Some(validModelAndView))
    contentModelBuilderService = new ContentModelBuilderService(viewFactory, jsonCallbackNameValidator, commonModelObjectsService, invalidModelBuilder, validModelBuilder)
  }

  @Test
  @throws[Exception]
  def shouldDelegateModelBuildingToTheFirstBuildWhoSaysTheyAreValid {
    assertEquals(validModelAndView, contentModelBuilderService.populateContentModel(request))
  }

  @Test
  @throws[Exception]
  def shouldReturnNullIfNoModelBuilderWasFoundForRequest {
    contentModelBuilderService = new ContentModelBuilderService(viewFactory, jsonCallbackNameValidator, commonModelObjectsService, invalidModelBuilder)
    assertNull(contentModelBuilderService.populateContentModel(request))
  }

  @Test
  @throws[Exception]
  def rssSuffixedRequestsShouldBeGivenTheRssView {
    when(viewFactory.getRssView(Mockito.anyString, Mockito.anyString, Mockito.anyString)).thenReturn(rssView)
    request.setPathInfo("/something/rss")
    assertEquals(rssView, contentModelBuilderService.populateContentModel(request).get.getView)
  }

  @Test
  @throws[Exception]
  def jsonSuffixedRequestsShouldBeGivenTheRssView {
    when(viewFactory.getJsonView).thenReturn(jsonView)
    request.setPathInfo("/something/json")
    assertEquals(jsonView, contentModelBuilderService.populateContentModel(request).get.getView)
  }

  @Test
  @throws[Exception]
  def jsonCallbackShouldBeAddedToJsonModelIfValid {
    when(viewFactory.getJsonView).thenReturn(jsonView)
    request.setPathInfo("/something/json")
    request.setParameter(CALLBACK, "validname")
    when(jsonCallbackNameValidator.isValidCallbackName(request.getParameter(CALLBACK))).thenReturn(true)
    val mv: ModelAndView = contentModelBuilderService.populateContentModel(request).get
    assertEquals("validname", mv.getModel.get(CALLBACK))
  }

  @Test
  @throws[Exception]
  def shouldRejectInvalidCallbackNames {
    when(viewFactory.getJsonView).thenReturn(jsonView)
    request.setPathInfo("/something/json")
    request.setParameter(CALLBACK, "Invalid name!")
    when(jsonCallbackNameValidator.isValidCallbackName(request.getParameter(CALLBACK))).thenReturn(false)
    val mv: ModelAndView = contentModelBuilderService.populateContentModel(request).get
    assertNull(mv.getModel.get(CALLBACK))
  }
}