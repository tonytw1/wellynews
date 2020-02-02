package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.controllers.CommonModelObjectsService
import nz.co.searchwellington.controllers.models.{ContentModelBuilderService, ModelBuilder}
import org.junit.Assert.assertEquals
import org.junit.{Before, Test}
import org.mockito.Matchers.anyString
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory
import uk.co.eelpieconsulting.common.views.json.JsonView
import uk.co.eelpieconsulting.common.views.rss.RssView

class ContentModelBuilderServiceTest {

  private val viewFactory = mock(classOf[ViewFactory])
  private val commonModelObjectsService = mock(classOf[CommonModelObjectsService])

  private var request: MockHttpServletRequest = null

  private[helpers] var validModelAndView: ModelAndView = mock(classOf[ModelAndView])

  private val invalidModelBuilder = mock(classOf[ModelBuilder])
  private val validModelBuilder =  mock(classOf[ModelBuilder])

  @Before def setup {
    request = new MockHttpServletRequest
    validModelAndView = new ModelAndView
    request.setPathInfo("/something")
  }

  @Test
  def shouldDelegateModelBuildingToTheFirstBuildWhoSaysTheyAreValid {
    when(invalidModelBuilder.isValid(request)).thenReturn(false)
    when(validModelBuilder.isValid(request)).thenReturn(true)
    when(validModelBuilder.populateContentModel(request)).thenReturn(Some(validModelAndView))

    val contentModelBuilderService = new ContentModelBuilderService(viewFactory,
      commonModelObjectsService,
      Seq(invalidModelBuilder, validModelBuilder)
    )

    assertEquals(Some(validModelAndView), contentModelBuilderService.populateContentModel(request))
  }

  @Test
  def shouldReturnNullIfNoModelBuilderWasFoundForRequest {
    when(invalidModelBuilder.isValid(request)).thenReturn(false)

    val contentModelBuilderService = new ContentModelBuilderService(viewFactory, commonModelObjectsService, Seq(invalidModelBuilder))

    assertEquals(None, contentModelBuilderService.populateContentModel(request))
  }

  @Test
  def rssSuffixedRequestsShouldBeGivenTheRssView {
    when(invalidModelBuilder.isValid(request)).thenReturn(false)
    when(validModelBuilder.isValid(request)).thenReturn(true)
    when(validModelBuilder.populateContentModel(request)).thenReturn(Some(validModelAndView))

    val contentModelBuilderService = new ContentModelBuilderService(
      viewFactory,
      commonModelObjectsService,
      Seq(invalidModelBuilder, validModelBuilder)
    )

    val rssView = mock(classOf[RssView])
    when(viewFactory.getRssView(anyString, anyString, anyString)).thenReturn(rssView)
    request.setPathInfo("/something/rss")
    assertEquals(rssView, contentModelBuilderService.populateContentModel(request).get.getView)
  }

  @Test
  def jsonSuffixedRequestsShouldBeGivenTheRssView {
    when(invalidModelBuilder.isValid(request)).thenReturn(false)
    when(validModelBuilder.isValid(request)).thenReturn(true)
    when(validModelBuilder.populateContentModel(request)).thenReturn(Some(validModelAndView))

    val contentModelBuilderService = new ContentModelBuilderService(
      viewFactory,
      commonModelObjectsService,
      Seq(invalidModelBuilder, validModelBuilder)
    )

    val jsonView = mock(classOf[JsonView])
    when(viewFactory.getJsonView).thenReturn(jsonView)
    request.setPathInfo("/something/json")
    assertEquals(jsonView, contentModelBuilderService.populateContentModel(request).get.getView)
  }

}
