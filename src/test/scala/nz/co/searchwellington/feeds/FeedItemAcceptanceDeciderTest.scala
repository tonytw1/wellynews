package nz.co.searchwellington.feeds

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{FeedAcceptancePolicy, Newsitem}
import nz.co.searchwellington.repositories.SuppressionDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, when}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class FeedItemAcceptanceDeciderTest extends ReasonableWaits {

  private val mongoRepository = mock(classOf[MongoRepository])
  private val suppressionDAO = mock(classOf[SuppressionDAO])

  private val feedItemAcceptanceDecider = new FeedItemAcceptanceDecider(mongoRepository, suppressionDAO)

  @Test
  def feeditemsWithNoProblemsShouldGenerateNoObjections(): Unit = {
    val feedItem = FeedItem(id = UUID.randomUUID().toString, title = Some("A feeditem"), url = "http://localhost/foo",
      date = Some(DateTime.now.toDate), subscriptionId = "")

    when(suppressionDAO.isSupressed(feedItem.url)).thenReturn(Future.successful(false))
    when(mongoRepository.getResourceByUrl(feedItem.url)).thenReturn(Future.successful(None))

    val objections = Await.result(feedItemAcceptanceDecider.getAcceptanceErrors(feedItem, FeedAcceptancePolicy.ACCEPT), TenSeconds)

    assertTrue(objections.isEmpty)
  }

  @Test
  def shouldRejectFeeditemsWhichHaveAlreadyBeenAccepted(): Unit = {
    val newsitem = FeedItem(id = UUID.randomUUID().toString, title = Some("A feeditem"), url = "http://localhost/foo", subscriptionId = "")

    when(suppressionDAO.isSupressed(newsitem.url)).thenReturn(Future.successful(false))
    when(mongoRepository.getResourceByUrl(newsitem.url)).thenReturn(Future.successful(Some(Newsitem())))

    val objections = Await.result(feedItemAcceptanceDecider.getAcceptanceErrors(newsitem, FeedAcceptancePolicy.ACCEPT), TenSeconds)

    assertTrue(objections.nonEmpty)
    assertEquals("Item already exists", objections.head)
  }

  @Test
  def shouldRejectFeeditemsWithSuppressedUrls(): Unit = {
    val newsitem = FeedItem(id = UUID.randomUUID().toString, title = Some("A feeditem"), url = "http://localhost/foo", subscriptionId = "")

    when(suppressionDAO.isSupressed(newsitem.url)).thenReturn(Future.successful(true))
    when(mongoRepository.getResourceByUrl(newsitem.url)).thenReturn(Future.successful(None))

    val objections = Await.result(feedItemAcceptanceDecider.getAcceptanceErrors(newsitem, FeedAcceptancePolicy.ACCEPT), TenSeconds)

    assertTrue(objections.nonEmpty)
    assertEquals("This item is suppressed", objections.head)
  }

  @Test
  def shouldRejectFeeditemsWithNoTitles(): Unit = {
    val newsitem = FeedItem(id = UUID.randomUUID().toString, url = "http://localhost/foo", subscriptionId = "")

    when(suppressionDAO.isSupressed(newsitem.url)).thenReturn(Future.successful(false))
    when(mongoRepository.getResourceByUrl(newsitem.url)).thenReturn(Future.successful(None))

    val objections = Await.result(feedItemAcceptanceDecider.getAcceptanceErrors(newsitem, FeedAcceptancePolicy.ACCEPT), TenSeconds)

    assertTrue(objections.nonEmpty)
    assertEquals("Item has no title", objections.head)
  }

  @Test
  def shouldRejectFeeditemsWithDatesWayInTheFuture(): Unit = {
    val newsitemWithFuturePublicationDate = FeedItem(id = UUID.randomUUID().toString, title = Some("A feeditem"),
      url = "http://localhost/foo", date = Some(DateTime.now.plusDays(10).toDate), subscriptionId = "")

    when(suppressionDAO.isSupressed(newsitemWithFuturePublicationDate.url)).thenReturn(Future.successful(false))
    when(mongoRepository.getResourceByUrl(newsitemWithFuturePublicationDate.url)).thenReturn(Future.successful(None))

    val objections = Await.result(feedItemAcceptanceDecider.getAcceptanceErrors(newsitemWithFuturePublicationDate, FeedAcceptancePolicy.ACCEPT), TenSeconds)

    assertTrue(objections.nonEmpty)
    assertEquals("This item has a date more than one week in the future", objections.head)
  }

}
