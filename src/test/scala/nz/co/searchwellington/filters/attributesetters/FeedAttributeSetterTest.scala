package nz.co.searchwellington.filters.attributesetters

import java.util.UUID

import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.repositories.HibernateResourceDAO
import org.junit.Assert.assertEquals
import org.junit.{Before, Test}
import org.mockito.Mockito.when
import org.mockito.{Mock, MockitoAnnotations}
import org.springframework.mock.web.MockHttpServletRequest

class FeedAttributeSetterTest {
  @Mock val resourceDAO: HibernateResourceDAO = null
  private val feed = Feed(id = UUID.randomUUID().toString, title = Some("Wellington City Council news"))
  private var request: MockHttpServletRequest = null
  private var feedAttributeSetter: FeedAttributeSetter = null

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)
    request = new MockHttpServletRequest
    when(resourceDAO.loadFeedByUrlWords("wcc-news")).thenReturn(Some(feed))
    feedAttributeSetter = new FeedAttributeSetter(resourceDAO)
  }

  @Test def shouldSetFeedAttributeForFeedPagePath(): Unit = {
    request.setPathInfo("/feed/wcc-news")
    feedAttributeSetter.setAttributes(request)
    assertEquals(feed, request.getAttribute("feedAttribute"))
  }

  @Test def shouldSetFeedAttributeForFeedEditPagePath(): Unit = {
    request.setPathInfo("/feed/wcc-news/edit")
    feedAttributeSetter.setAttributes(request)
    assertEquals(feed, request.getAttribute("feedAttribute"))
  }

  @Test def shouldSetFeedAttributeForFeedSavePath(): Unit = {
    request.setPathInfo("/feed/wcc-news/save")
    feedAttributeSetter.setAttributes(request)
    assertEquals(feed, request.getAttribute("feedAttribute"))
  }

}
