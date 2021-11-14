package nz.co.searchwellington.filters.attributesetters

import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Assert.assertEquals
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FeedAttributeSetterTest {
  private val mongoRepository = mock(classOf[MongoRepository])
  private val feed = Feed(id = UUID.randomUUID().toString, title = Some("Wellington City Council news"))
  private val request= new MockHttpServletRequest

  private val feedAttributeSetter = new FeedAttributeSetter(mongoRepository)

  @Before def setup(): Unit = {
    when(mongoRepository.getFeedByUrlwords("wcc-news")).thenReturn(Future.successful(Some(feed)))
  }

  @Test def shouldSetFeedAttributeForFeedPagePath(): Unit = {
    request.setRequestURI("/feed/wcc-news")
    feedAttributeSetter.setAttributes(request)
    assertEquals(feed, request.getAttribute("feedAttribute"))
  }

  @Test def shouldSetFeedAttributeForFeedEditPagePath(): Unit = {
    request.setRequestURI("/feed/wcc-news/edit")
    feedAttributeSetter.setAttributes(request)
    assertEquals(feed, request.getAttribute("feedAttribute"))
  }

  @Test def shouldSetFeedAttributeForFeedSavePath(): Unit = {
    request.setRequestURI("/feed/wcc-news/save")
    feedAttributeSetter.setAttributes(request)
    assertEquals(feed, request.getAttribute("feedAttribute"))
  }

}
