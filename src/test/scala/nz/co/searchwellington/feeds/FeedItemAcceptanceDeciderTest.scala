package nz.co.searchwellington.feeds

import java.util.UUID

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.reading.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, Newsitem}
import nz.co.searchwellington.repositories.SuppressionDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.utils.UrlCleaner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.{mock, when}

import scala.concurrent.{Await, Future}

class FeedItemAcceptanceDeciderTest extends ReasonableWaits {

  private val mongoRepository = mock(classOf[MongoRepository])
  private val suppressionDAO = mock(classOf[SuppressionDAO])
  private val urlCleaner = mock(classOf[UrlCleaner])

  private val feedItemAcceptanceDecider = new FeedItemAcceptanceDecider(mongoRepository, suppressionDAO, urlCleaner)

  @Test
  def feeditemsWithNoProblemsShouldGenerateNoObjections() = {
    val feed = Feed(acceptance = FeedAcceptancePolicy.ACCEPT)
    val feedItem = FeedItem(id = UUID.randomUUID().toString, title = Some("A feeditem"), subscriptionId = UUID.randomUUID().toString, url = "http://localhost/foo")

    when(urlCleaner.cleanSubmittedItemUrl(feedItem.url)).thenReturn(feedItem.url)
    when(suppressionDAO.isSupressed(feedItem.url)).thenReturn(Future.successful(false))
    when(mongoRepository.getResourceByUrl(feedItem.url)).thenReturn(Future.successful(None))

    val objections = Await.result(feedItemAcceptanceDecider.getAcceptanceErrors(feed, feedItem, feed.acceptance), TenSeconds)

    assertTrue(objections.isEmpty)
  }

  @Test
  def shouldRejectFeeditemsWhichHaveAlreadyBeenAccepted() = {
    val feed = Feed(acceptance = FeedAcceptancePolicy.ACCEPT)
    val feedItem = FeedItem(id = UUID.randomUUID().toString, title = Some("A feeditem"), subscriptionId = UUID.randomUUID().toString, url = "http://localhost/foo")

    when(urlCleaner.cleanSubmittedItemUrl(feedItem.url)).thenReturn(feedItem.url)
    when(suppressionDAO.isSupressed(feedItem.url)).thenReturn(Future.successful(false))
    when(mongoRepository.getResourceByUrl(feedItem.url)).thenReturn(Future.successful(Some(Newsitem())))

    val objections = Await.result(feedItemAcceptanceDecider.getAcceptanceErrors(feed, feedItem, feed.acceptance), TenSeconds)

    assertTrue(objections.nonEmpty)
    assertEquals("Item already exists", objections.head)
  }

}
