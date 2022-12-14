package nz.co.searchwellington.filters.attributesetters

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class FeedAttributeSetterTest extends ReasonableWaits{
  private val mongoRepository = mock(classOf[MongoRepository])
  private val feed = Feed(id = UUID.randomUUID().toString, title = "Wellington City Council news")
  private val request = new MockHttpServletRequest

  private val feedAttributeSetter = new FeedAttributeSetter(mongoRepository)

  @BeforeEach
  def setup(): Unit = {
    when(mongoRepository.getFeedByUrlwords("wcc-news")).thenReturn(Future.successful(Some(feed)))
  }

  @Test def shouldSetFeedAttributeForFeedPagePath(): Unit = {
    request.setRequestURI("/feed/wcc-news")

    val attributes = Await.result(feedAttributeSetter.setAttributes(request), TenSeconds)

    assertEquals(feed, attributes("feedAttribute"))
  }

  @Test def shouldSetFeedAttributeForFeedEditPagePath(): Unit = {
    request.setRequestURI("/feed/wcc-news/edit")

    val attributes = Await.result(feedAttributeSetter.setAttributes(request), TenSeconds)

    assertEquals(feed, attributes("feedAttribute"))
  }

  @Test def shouldSetFeedAttributeForFeedSavePath(): Unit = {
    request.setRequestURI("/feed/wcc-news/save")

    val attributes = Await.result(feedAttributeSetter.setAttributes(request), TenSeconds)

    assertEquals(feed, attributes("feedAttribute"))
  }

  @Test def shouldSetFeedAttributeForAcceptAll(): Unit = {
    request.setRequestURI("/feed/wcc-news/accept-all")

    val attributes = Await.result(feedAttributeSetter.setAttributes(request), TenSeconds)

    assertEquals(feed, attributes("feedAttribute"))
  }

}
