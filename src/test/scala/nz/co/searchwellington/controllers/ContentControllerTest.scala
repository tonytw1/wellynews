package nz.co.searchwellington.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.controllers.models.{ContentModelBuilderService, ContentModelBuilderServiceFactory}
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.{mock, verify, verifyZeroInteractions, when}
import org.springframework.web.servlet.ModelAndView

class ContentControllerTest {
  private val contentModelBuilderServiceFactory = mock(classOf[ContentModelBuilderServiceFactory])
  private val contentModelBuilderService = mock(classOf[ContentModelBuilderService])
  private val urlStack = mock(classOf[UrlStack])

  private val request = mock(classOf[HttpServletRequest])
  private val unknownPathRequest = mock(classOf[HttpServletRequest])
  private val response = mock(classOf[HttpServletResponse])

  private def contentController = new ContentController(contentModelBuilderServiceFactory, urlStack)

  @Test
  def shouldDelegateToTheContentModelBuilderToGetTheModelForThisRequest() {
    val expectedModelAndView = new ModelAndView("a-view") // TODO mock
    when(contentModelBuilderServiceFactory.makeContentModelBuilderService()).thenReturn(contentModelBuilderService)
    when(contentModelBuilderService.populateContentModel(request)).thenReturn(Some(expectedModelAndView))

    val modelAndView = contentController.normal(request, response)

    assertEquals(expectedModelAndView, modelAndView)
  }

  @Test
  def should404IfNotModelWasAvailableForThisRequest() {
    when(contentModelBuilderServiceFactory.makeContentModelBuilderService()).thenReturn(contentModelBuilderService)
    when(contentModelBuilderService.populateContentModel(unknownPathRequest)).thenReturn(None)

    contentController.normal(unknownPathRequest, response)

    verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND)
  }

  @Test
  def shouldNotPush404sOntoTheReturnToUrlStack() {
    when(contentModelBuilderServiceFactory.makeContentModelBuilderService()).thenReturn(contentModelBuilderService)
    when(contentModelBuilderService.populateContentModel(unknownPathRequest)).thenReturn(None)

    contentController.normal(unknownPathRequest, response)

    verifyZeroInteractions(urlStack)
  }

  @Test
  def htmlPageViewsShouldBePutOntoTheUrlStack() {
    val expectedModelAndView: ModelAndView = new ModelAndView("a-view")
    when(contentModelBuilderServiceFactory.makeContentModelBuilderService()).thenReturn(contentModelBuilderService)
    when(contentModelBuilderService.populateContentModel(request)).thenReturn(Some(expectedModelAndView))

    contentController.normal(request, response)

    verify(urlStack).setUrlStack(request)
  }

}