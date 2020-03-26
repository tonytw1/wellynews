package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Website
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.junit.Assert.{assertFalse, assertTrue}
import org.junit.Test
import org.mockito.Mockito.mock
import org.springframework.mock.web.MockHttpServletRequest

class PublisherMonthModelBuilderTest extends ReasonableWaits {

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])

  private val aPublisher = Website(title = Some("A publisher"), url_words = Some("a-publisher"))

  private val modelBuilder = new PublisherMonthModelBuilder(contentRetrievalService)

  @Test
  def isValidForPublisherAndMonthPath(): Unit = {
    val request = new MockHttpServletRequest()
    request.setAttribute("publisher", aPublisher)
    request.setContextPath("/a-publisher/2020 feb")

    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def isNotValidForPublisherAndMonthPath(): Unit = {
    val archiveMonthRequest = new MockHttpServletRequest
    archiveMonthRequest.setContextPath("/2020 may")

    assertFalse(modelBuilder.isValid(archiveMonthRequest))
  }

  @Test
  def isValidForPublisherNonDate(): Unit = {
    val request = new MockHttpServletRequest()
    request.setAttribute("publisher", aPublisher)
    request.setContextPath("/a-publisher/something")

    assertFalse(modelBuilder.isValid(request))
  }

}
