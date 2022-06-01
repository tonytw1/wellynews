package nz.co.searchwellington.feeds

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.WhakaokoFeedReader
import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, Newsitem, Resource, User}
import nz.co.searchwellington.modification.ContentUpdateService
import org.junit.Assert.{assertEquals, assertTrue, fail}
import org.junit.Test
import org.mockito.{ArgumentCaptor, Matchers}
import org.mockito.Mockito.{mock, verify, when}

import java.util.UUID
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class FeedReaderTest extends ReasonableWaits {

  private val feedItemAcceptanceDecider = mock(classOf[FeedItemAcceptanceDecider])
  private val contentUpdateService = mock(classOf[ContentUpdateService])
  private val feedReaderUpdateService=  mock(classOf[FeedReaderUpdateService])
  private val whakaokoFeedReader =  mock(classOf[WhakaokoFeedReader])
  private val feeditemToNewsItemService=  mock(classOf[FeeditemToNewsitemService])

  private val suggestedFeed = Feed(acceptance = FeedAcceptancePolicy.SUGGEST)
  private val loggedInUser = User()

  private val feedReader = new FeedReader(feedItemAcceptanceDecider, contentUpdateService, feedReaderUpdateService, whakaokoFeedReader, feeditemToNewsItemService)


  @Test
  def shouldNotAcceptNewsitemsFromSuggestOnlyFeeds(): Unit = {
    val feedItems= Seq(FeedItem(id = UUID.randomUUID().toString, subscriptionId = UUID.randomUUID().toString, url = "http://localhost/1"))
    when(whakaokoFeedReader.fetchFeedItems(suggestedFeed)).thenReturn(Future.successful(Right((feedItems, feedItems.size))))
    when(contentUpdateService.update(Matchers.any(classOf[Feed]))(Matchers.any())).thenReturn(Future.successful(true))

    val result = Await.result(feedReader.processFeed(suggestedFeed, loggedInUser), TenSeconds)

    assertEquals(0, result)
    val updatedFeed = ArgumentCaptor.forClass(classOf[Feed])
    verify(contentUpdateService).update(updatedFeed.capture)(Matchers.any())
    assertTrue(updatedFeed.getValue.last_read.nonEmpty)
  }

  @Test
  def shouldUpdateLastReadAndLatestItemForSuggestOnlyFeeds(): Unit = {
    feedReader.processFeed(suggestedFeed, loggedInUser)

    fail()
  }

}