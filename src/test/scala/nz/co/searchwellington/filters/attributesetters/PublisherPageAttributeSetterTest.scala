package nz.co.searchwellington.filters.attributesetters

import java.util.UUID

import nz.co.searchwellington.model.Website
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Assert.assertEquals
import org.junit.{Before, Test}
import org.mockito.Mockito.when
import org.mockito.{Mock, MockitoAnnotations}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PublisherPageAttributeSetterTest {
  @Mock val mongoRepository: MongoRepository = null
  private val publisher = Website(id = UUID.randomUUID().toString, title = Some("Wellington City Council"))
  private var request: MockHttpServletRequest = null
  private var pageAttributeSetter: PublisherPageAttributeSetter = null

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)
    request = new MockHttpServletRequest
    when(mongoRepository.getWebsiteByUrlwords("wellington-city-council")).thenReturn(Future.successful(Some(publisher)))
    pageAttributeSetter = new PublisherPageAttributeSetter(mongoRepository)
  }

  @Test def shouldSetPublisherAttributeForPublisherPath(): Unit = {
    request.setRequestURI("/wellington-city-council")

    pageAttributeSetter.setAttributes(request)

    assertEquals(publisher, request.getAttribute("publisher"))
  }


  @Test def shouldSetPublisherAttributeForPublisherArchivePath(): Unit = {
    request.setRequestURI("/wellington-city-council/2020-mar")

    pageAttributeSetter.setAttributes(request)

    assertEquals(publisher, request.getAttribute("publisher"))
  }

  @Test def shouldSetPublisherAttributeForPublisherRssPath(): Unit = {
    request.setRequestURI("/wellington-city-council/rss")

    pageAttributeSetter.setAttributes(request)

    assertEquals(publisher, request.getAttribute("publisher"))
  }

}
