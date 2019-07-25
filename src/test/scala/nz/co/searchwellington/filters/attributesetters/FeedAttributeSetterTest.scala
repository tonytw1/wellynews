package nz.co.searchwellington.filters.attributesetters

import java.util.UUID

import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Assert.assertEquals
import org.junit.{Before, Test}
import org.mockito.Mockito.when
import org.mockito.{Mock, MockitoAnnotations}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class FeedAttributeSetterTest {
  @Mock val mongoRepository: MongoRepository = null
  private val feed = Feed(id = UUID.randomUUID().toString, title = Some("Wellington City Council news"))
  private var request: MockHttpServletRequest = null
  private var feedAttributeSetter: FeedAttributeSetter = null

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)
    request = new MockHttpServletRequest
    when(mongoRepository.getFeedByUrlwords("wcc-news")).thenReturn(Future.successful(Some(feed)))
    feedAttributeSetter = new FeedAttributeSetter(mongoRepository)
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
