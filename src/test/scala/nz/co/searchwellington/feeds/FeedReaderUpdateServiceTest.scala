package nz.co.searchwellington.feeds

import io.micrometer.core.instrument.{Counter, MeterRegistry}
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.taggingvotes.HandTagging
import nz.co.searchwellington.model.{Feed, Newsitem, Tag, Tagging, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.tagging.AutoTaggingService
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.{mock, when}

import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class FeedReaderUpdateServiceTest extends ReasonableWaits {

  private val contentUpdateService = mock(classOf[ContentUpdateService])
  private val autoTaggingService = mock(classOf[AutoTaggingService])
  private val feedItemAcceptor = mock(classOf[FeedItemAcceptor])
  private val meterRegistry = mock(classOf[MeterRegistry])
  private val counter = mock(classOf[Counter])

  private val feedReaderUpdateService = new FeedReaderUpdateService(contentUpdateService,
    autoTaggingService, feedItemAcceptor, meterRegistry)

  @Test
  def acceptedFeedItemsShouldBeAutotaggedAndSaved(): Unit = {
    val feedReaderUser = User()
    val autoTagUser = User()

    val feednewsitem = FeedItem(id = "123", url = "http://localhost/123", subscriptionId = "123")
    val feed = Feed()

    val atag = Tag(id = "atag")
    val anotherTag = Tag(id = "anothertag")

    val acceptedNewsitem = Newsitem()
    val autoTaggings = Set(
      HandTagging(tag = atag, user = autoTagUser),
      HandTagging(tag = anotherTag, user = autoTagUser)
    )

    when(feedItemAcceptor.acceptFeedItem(feedReaderUser, (feednewsitem, feed))).thenReturn(acceptedNewsitem)
    when(autoTaggingService.autotag(acceptedNewsitem.copy(held = false))).thenReturn(Future.successful(autoTaggings))
    when(contentUpdateService.create(acceptedNewsitem.copy(held = false,
      resource_tags = Seq(
        Tagging(tag_id = atag._id, user_id = autoTagUser._id),
        Tagging(tag_id = anotherTag._id, user_id = autoTagUser._id),
      )
    ))).thenReturn(Future.successful(acceptedNewsitem))
    when(meterRegistry.counter("feedreader_accepted")).thenReturn(counter)

    val created = Await.result(feedReaderUpdateService.acceptFeeditem(feedReaderUser, feednewsitem, feed), TenSeconds)

    assertEquals(created, acceptedNewsitem)
  }

}
