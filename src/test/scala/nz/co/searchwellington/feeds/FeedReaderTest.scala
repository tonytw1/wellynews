package nz.co.searchwellington.feeds

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.WhakaokoFeedReader
import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, User}
import nz.co.searchwellington.modification.ContentUpdateService
import org.joda.time.DateTime
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test
import org.mockito.Mockito.{mock, verify, when}
import org.mockito.{ArgumentCaptor, Matchers}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class FeedReaderTest extends ReasonableWaits {

  private val feedItemAcceptanceDecider = mock(classOf[FeedItemAcceptanceDecider])
  private val contentUpdateService = mock(classOf[ContentUpdateService])
  private val feedReaderUpdateService = mock(classOf[FeedReaderUpdateService])
  private val whakaokoFeedReader = mock(classOf[WhakaokoFeedReader])
  private val feeditemToNewsItemService = mock(classOf[FeeditemToNewsitemService])

  private val suggestedFeed = Feed(acceptance = FeedAcceptancePolicy.SUGGEST)
  private val loggedInUser = User()

  private val feedReader = new FeedReader(feedItemAcceptanceDecider, contentUpdateService, feedReaderUpdateService, whakaokoFeedReader, feeditemToNewsItemService)


  @Test
  def shouldNotAcceptNewsitemsFromSuggestOnlyFeeds(): Unit = {
    val feedItems = Seq(FeedItem(id = UUID.randomUUID().toString, subscriptionId = UUID.randomUUID().toString, url = "http://localhost/1"))
    when(whakaokoFeedReader.fetchFeedItems(suggestedFeed)).thenReturn(Future.successful(Right((feedItems, feedItems.size))))
    when(contentUpdateService.update(Matchers.any(classOf[Feed]))(Matchers.any())).thenReturn(Future.successful(true))

    val result = Await.result(feedReader.processFeed(suggestedFeed, loggedInUser), TenSeconds)

    assertEquals(0, result)
  }

  @Test
  def shouldUpdateLastReadAndLatestItemForSuggestOnlyFeeds(): Unit = {
    val firstFeedItem = FeedItem(id = UUID.randomUUID().toString, subscriptionId = UUID.randomUUID().toString,
      url = "http://localhost/1", date = Some(DateTime.now))
    val feedItems = Seq(firstFeedItem)
    when(whakaokoFeedReader.fetchFeedItems(suggestedFeed)).thenReturn(Future.successful(Right((feedItems, feedItems.size))))
    when(contentUpdateService.update(Matchers.any(classOf[Feed]))(Matchers.any())).thenReturn(Future.successful(true))

    Await.result(feedReader.processFeed(suggestedFeed, loggedInUser), TenSeconds)

    val updatedFeed = ArgumentCaptor.forClass(classOf[Feed])
    verify(contentUpdateService).update(updatedFeed.capture)(Matchers.any())
    assertTrue(updatedFeed.getValue.last_read.nonEmpty)
    assertEquals(firstFeedItem.date.map(_.toDate), updatedFeed.getValue.latestItemDate)
  }

}