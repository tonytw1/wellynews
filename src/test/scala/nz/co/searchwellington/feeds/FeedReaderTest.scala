package nz.co.searchwellington.feeds

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.WhakaokoFeedReader
import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, User}
import nz.co.searchwellington.modification.ContentUpdateService
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, verify, when}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class FeedReaderTest extends ReasonableWaits {

  private val feedItemAcceptanceDecider = mock(classOf[FeedItemAcceptanceDecider])
  private val contentUpdateService = mock(classOf[ContentUpdateService])
  private val feedReaderUpdateService = mock(classOf[FeedReaderUpdateService])
  private val whakaokoFeedReader = mock(classOf[WhakaokoFeedReader])

  private val suggestedFeed = Feed(acceptance = FeedAcceptancePolicy.SUGGEST)
  private val loggedInUser = User()

  private val firstFeedItem = FeedItem(id = UUID.randomUUID().toString, subscriptionId = UUID.randomUUID().toString,
    url = "http://localhost/1", date = Some(DateTime.now.toDate))
  private val feedItems = Seq(firstFeedItem)

  private implicit val currentSpan = Span.current()

  private val feedReader = new FeedReader(feedItemAcceptanceDecider, contentUpdateService, feedReaderUpdateService, whakaokoFeedReader)

  @Test
  def shouldNotAcceptNewsitemsFromSuggestOnlyFeeds(): Unit = {
    when(whakaokoFeedReader.fetchFeedItems(suggestedFeed)).thenReturn(Future.successful(Right((feedItems, feedItems.size))))
    when(contentUpdateService.update(any(classOf[Feed]))(any())).thenReturn(Future.successful(true))

    val result = Await.result(feedReader.processFeed(suggestedFeed, loggedInUser), TenSeconds)

    assertEquals(0, result)
  }

  @Test
  def shouldUpdateLastReadAndLatestItemForSuggestOnlyFeeds(): Unit = {
    when(whakaokoFeedReader.fetchFeedItems(suggestedFeed)).thenReturn(Future.successful(Right((feedItems, feedItems.size))))
    when(contentUpdateService.update(any(classOf[Feed]))(any())).thenReturn(Future.successful(true))

    Await.result(feedReader.processFeed(suggestedFeed, loggedInUser), TenSeconds)

    val updatedFeed: ArgumentCaptor[Feed] = ArgumentCaptor.forClass(classOf[Feed])
    verify(contentUpdateService).update(updatedFeed.capture)(any())
    assertTrue(updatedFeed.getValue.last_read.nonEmpty)
    assertEquals(firstFeedItem.date, updatedFeed.getValue.latestItemDate)
  }

}