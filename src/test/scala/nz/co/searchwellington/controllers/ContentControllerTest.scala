package nz.co.searchwellington.controllers

import org.junit.Assert.assertEquals
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import nz.co.searchwellington.controllers.models.ContentModelBuilderService
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.springframework.web.servlet.ModelAndView

class ContentControllerTest {
  @Mock private[controllers] val contentModelBuilderService: ContentModelBuilderService = null
  @Mock private[controllers] val urlStack: UrlStack = null
  private val request: HttpServletRequest = null
  private val unknownPathRequest: HttpServletRequest = null
  @Mock private[controllers] val response: HttpServletResponse = null
  private var contentController: ContentController = null

  @Before def setup {
    MockitoAnnotations.initMocks(this)
    contentController = new ContentController(contentModelBuilderService, urlStack)
  }

  @Test
  @throws[Exception]
  def shouldDelegateTotTheContentModelBuilderToGetTheModelForThisRequest {
    val expectedModelAndView: ModelAndView = new ModelAndView("a-view")
    Mockito.when(contentModelBuilderService.populateContentModel(request)).thenReturn(Some(expectedModelAndView))
    assertEquals(expectedModelAndView, contentController.normal(request, response))
  }

  @Test
  @throws[Exception]
  def should404IfNotModelWasAvailableForThisRequest {
    Mockito.when(contentModelBuilderService.populateContentModel(unknownPathRequest)).thenReturn(None)
    contentController.normal(unknownPathRequest, response)
    Mockito.verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND)
  }

  @Test
  @throws[Exception]
  def shouldNotPush404sOntoTheReturnToUrlStack {
    Mockito.when(contentModelBuilderService.populateContentModel(unknownPathRequest)).thenReturn(None)
    contentController.normal(unknownPathRequest, response)
    Mockito.verifyZeroInteractions(urlStack)
  }

  @Test
  @throws[Exception]
  def htmlPageViewsShouldBePutOntoTheUrlStack {
    val expectedModelAndView: ModelAndView = new ModelAndView("a-view")
    Mockito.when(contentModelBuilderService.populateContentModel(request)).thenReturn(Some(expectedModelAndView))
    contentController.normal(request, response)
    Mockito.verify(urlStack).setUrlStack(request)
  }
}