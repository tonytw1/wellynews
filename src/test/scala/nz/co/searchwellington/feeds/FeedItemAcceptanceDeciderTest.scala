package nz.co.searchwellington.feeds

import java.util.UUID

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{FeedAcceptancePolicy, Newsitem}
import nz.co.searchwellington.repositories.SuppressionDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.utils.UrlCleaner
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test
import org.mockito.Mockito.{mock, when}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class FeedItemAcceptanceDeciderTest extends ReasonableWaits {

  private val mongoRepository = mock(classOf[MongoRepository])
  private val suppressionDAO = mock(classOf[SuppressionDAO])
  private val urlCleaner = mock(classOf[UrlCleaner])

  private val feedItemAcceptanceDecider = new FeedItemAcceptanceDecider(mongoRepository, suppressionDAO, urlCleaner)

  @Test
  def feeditemsWithNoProblemsShouldGenerateNoObjections() = {
    val feedItem = FeedItem(id = UUID.randomUUID().toString, title = Some("A feeditem"), subscriptionId = UUID.randomUUID().toString, url = "http://localhost/foo")

    when(urlCleaner.cleanSubmittedItemUrl(feedItem.url)).thenReturn(feedItem.url)
    when(suppressionDAO.isSupressed(feedItem.url)).thenReturn(Future.successful(false))
    when(mongoRepository.getResourceByUrl(feedItem.url)).thenReturn(Future.successful(None))

    val objections = Await.result(feedItemAcceptanceDecider.getAcceptanceErrors(feedItem, FeedAcceptancePolicy.ACCEPT), TenSeconds)

    assertTrue(objections.isEmpty)
  }

  @Test
  def shouldRejectFeeditemsWhichHaveAlreadyBeenAccepted() = {
    val feedItem = FeedItem(id = UUID.randomUUID().toString, title = Some("A feeditem"), subscriptionId = UUID.randomUUID().toString, url = "http://localhost/foo")

    when(urlCleaner.cleanSubmittedItemUrl(feedItem.url)).thenReturn(feedItem.url)
    when(suppressionDAO.isSupressed(feedItem.url)).thenReturn(Future.successful(false))
    when(mongoRepository.getResourceByUrl(feedItem.url)).thenReturn(Future.successful(Some(Newsitem())))

    val objections = Await.result(feedItemAcceptanceDecider.getAcceptanceErrors(feedItem, FeedAcceptancePolicy.ACCEPT), TenSeconds)

    assertTrue(objections.nonEmpty)
    assertEquals("Item already exists", objections.head)
  }

  @Test
  def shouldRejectFeeditemsWithSuppressedUrls() = {
    val feedItem = FeedItem(id = UUID.randomUUID().toString, title = Some("A feeditem"), subscriptionId = UUID.randomUUID().toString, url = "http://localhost/foo")

    when(urlCleaner.cleanSubmittedItemUrl(feedItem.url)).thenReturn(feedItem.url)
    when(suppressionDAO.isSupressed(feedItem.url)).thenReturn(Future.successful(true))
    when(mongoRepository.getResourceByUrl(feedItem.url)).thenReturn(Future.successful(None))

    val objections = Await.result(feedItemAcceptanceDecider.getAcceptanceErrors(feedItem, FeedAcceptancePolicy.ACCEPT), TenSeconds)

    assertTrue(objections.nonEmpty)
    assertEquals("This item is suppressed", objections.head)
  }

}
