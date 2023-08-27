package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.frontend.{FrontendNewsitem, FrontendResource}
import nz.co.searchwellington.model.helpers.ArchiveLinksService
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.{RssUrlBuilder, UrlBuilder}
import org.joda.time.{DateTime, Duration}
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.springframework.mock.web.MockHttpServletRequest

import java.util
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

class IndexModelBuilderTest extends ReasonableWaits with ContentFields {

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val rssUrlBuilder = mock(classOf[RssUrlBuilder])
  private val urlBuilder = mock(classOf[UrlBuilder])
  private val archiveLinksService = mock(classOf[ArchiveLinksService])
  private val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])

  private val request = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/")
    request
  }

  private val latestNewsitems = {
    Range.inclusive(1, 30).map { i =>
      FrontendNewsitem(id = i.toString, name = s"Newsitem title $i", date = Some(DateTime.now.minusHours(i).toDate))
    }
  }

  private val loggedInUser = None

  private implicit val currentSpan: Span = Span.current()

  private val modelBuilder = new IndexModelBuilder(contentRetrievalService, rssUrlBuilder, urlBuilder, archiveLinksService, commonAttributesModelBuilder)

  @Test
  def isValidForHomePageUrl(): Unit = {
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def isValidForMainRssUrl(): Unit = {
    request.setRequestURI("/rss")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def isValidForMainJsonUrl(): Unit = {
    request.setRequestURI("/json")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def indexPageMainContentIsTheLatestNewsitems(): Unit = {
    when(contentRetrievalService.getLatestNewsitems(30, loggedInUser)).thenReturn(Future.successful(latestNewsitems))
    when(contentRetrievalService.getAcceptedNewsitems(
      ArgumentMatchers.eq(30),
      ArgumentMatchers.eq(loggedInUser),
      ArgumentMatchers.eq(None),
      ArgumentMatchers.any(),
      ArgumentMatchers.any()
    )(any, any)).
      thenReturn(Future.successful((Seq.empty, 0L)))
    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(latestNewsitems.asJava, mv.get(MAIN_CONTENT))
  }

  @Test
  def recentlyAcceptedItemsLessThanOneWeekOldArePrioritisedSoThatTheyArePickedUpByFeedReaders(): Unit = {
    val recentlyAcceptedItem = FrontendNewsitem(id = "123", name = s"Recently accepted but older publication date",
      date = Some(new DateTime(latestNewsitems.last.date.get).minusDays(1).toDate), accepted = Some(DateTime.now.toDate))

    when(contentRetrievalService.getAcceptedNewsitems(
      ArgumentMatchers.eq(30),
      ArgumentMatchers.eq(loggedInUser),
      ArgumentMatchers.eq(None),
      ArgumentMatchers.any(),
      ArgumentMatchers.any()
    )(any, any)).
      thenReturn(Future.successful((Seq(recentlyAcceptedItem), 1L)))

    when(contentRetrievalService.getLatestNewsitems(30, loggedInUser)).thenReturn(Future.successful(latestNewsitems))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    val mainContent = mv.get(MAIN_CONTENT).asInstanceOf[util.List[FrontendResource]].asScala.toSeq

    val acceptedAfter: ArgumentCaptor[Option[DateTime]] = ArgumentCaptor.forClass(classOf[Option[DateTime]])
    val publishedAfter: ArgumentCaptor[Option[DateTime]] = ArgumentCaptor.forClass(classOf[Option[DateTime]])

    verify(contentRetrievalService).getAcceptedNewsitems(
      ArgumentMatchers.eq(30),
      ArgumentMatchers.eq(loggedInUser),
      ArgumentMatchers.eq(None),
      acceptedAfter.capture(),
      publishedAfter.capture()
    )(any, any)

    assertTrue(Math.abs(new Duration(DateTime.now.minusDays(1), acceptedAfter.getValue.get).getMillis) < 1000)
    assertTrue(Math.abs(new Duration(DateTime.now.minusWeeks(2), publishedAfter.getValue.get).getMillis) < 1000)

    assertTrue(mainContent.contains(recentlyAcceptedItem))
    assertEquals(31L, mainContent.size) // TODO we can live with this
  }

}
