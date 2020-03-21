package nz.co.searchwellington.feeds

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.reading.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, Newsitem, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.tagging.AutoTaggingService
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.{mock, when}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class FeedReaderUpdateServiceTest extends ReasonableWaits {

  private val contentUpdateService = mock(classOf[ContentUpdateService])
  private val autoTaggingService = mock(classOf[AutoTaggingService])
  private val feedItemAcceptor = mock(classOf[FeedItemAcceptor])
  private val linkCheckerQueue = mock(classOf[LinkCheckerQueue])

  private val feedReaderUpdateService = new FeedReaderUpdateService(contentUpdateService, autoTaggingService, feedItemAcceptor, linkCheckerQueue)

  @Test
  def acceptedFeedItemsShouldBeAutotaggedAndSaved(): Unit = {
    val feedReaderUser = User()
    val feednewsitem = FeedItem(id = "123", url = "http://localhost/123", subscriptionId = "123")
    val feed: Feed = Feed()

    val acceptedNewsitem = Newsitem()
    when(feedItemAcceptor.acceptFeedItem(feedReaderUser, (feednewsitem, feed))).thenReturn(acceptedNewsitem)
    when(autoTaggingService.autotag(acceptedNewsitem.copy(held = false))).thenReturn(Future.successful(List.empty))
    when(contentUpdateService.create(acceptedNewsitem.copy(held = false))).thenReturn(Future.successful(acceptedNewsitem))

    val created = Await.result(feedReaderUpdateService.acceptFeeditem(feedReaderUser, feednewsitem, feed), TenSeconds)

    assertEquals(created, acceptedNewsitem)
  }

}
