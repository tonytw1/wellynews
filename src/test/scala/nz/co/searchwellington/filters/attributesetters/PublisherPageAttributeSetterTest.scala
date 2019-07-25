package nz.co.searchwellington.filters.attributesetters

import java.util.UUID

import nz.co.searchwellington.model.Website
import nz.co.searchwellington.repositories.HibernateResourceDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Assert.assertEquals
import org.junit.{Before, Test}
import org.mockito.Mockito.when
import org.mockito.{Mock, MockitoAnnotations}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

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
    request.setPathInfo("/wellington-city-council")

    pageAttributeSetter.setAttributes(request)

    assertEquals(publisher, request.getAttribute("publisher"))
  }

  @Test def shouldSetPublisherAttributeForPublisherEditPath(): Unit = {
    request.setPathInfo("/wellington-city-council/edit")

    pageAttributeSetter.setAttributes(request)

    assertEquals(publisher, request.getAttribute("publisher"))
  }

  @Test def shouldSetPublisherAttributeForPublisherRssPath(): Unit = {
    request.setPathInfo("/wellington-city-council/rss")

    pageAttributeSetter.setAttributes(request)

    assertEquals(publisher, request.getAttribute("publisher"))
  }

}
