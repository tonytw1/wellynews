package nz.co.searchwellington.filters.attributesetters

import nz.co.searchwellington.model.Website
import nz.co.searchwellington.repositories.HibernateResourceDAO
import org.junit.Assert.assertEquals
import org.junit.{Before, Test}
import org.mockito.Mockito.when
import org.mockito.{Mock, MockitoAnnotations}
import org.springframework.mock.web.MockHttpServletRequest

class PublisherPageAttributeSetterTest {
  @Mock val resourceDAO: HibernateResourceDAO = null
  private val publisher = Website(title = Some("Wellington City Council"))
  private var request: MockHttpServletRequest = null
  private var pageAttributeSetter: PublisherPageAttributeSetter = null

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)
    request = new MockHttpServletRequest
    when(resourceDAO.getPublisherByUrlWords("wellington-city-council")).thenReturn(Some(publisher))
    pageAttributeSetter = new PublisherPageAttributeSetter(resourceDAO)
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
