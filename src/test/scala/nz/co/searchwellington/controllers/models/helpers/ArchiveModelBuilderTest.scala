package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.frontend.FrontendNewsitem
import nz.co.searchwellington.model.helpers.ArchiveLinksService
import nz.co.searchwellington.model.{ArchiveLink, SiteInformation}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.RssUrlBuilder
import org.joda.time.{DateTime, DateTimeZone, Interval}
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import uk.co.eelpieconsulting.common.dates.DateFormatter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

class ArchiveModelBuilderTest extends ReasonableWaits with ContentFields with TestArchiveLinks {

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val request = new MockHttpServletRequest

  private val newsitem = FrontendNewsitem(id = "123")
  private val anotherNewsitem = FrontendNewsitem(id = "456")
  private val monthNewsitems = Seq(newsitem, anotherNewsitem)

  private val loggedInUser = None

  private implicit val currentSpan: Span = Span.current()

  val modelBuilder =  new ArchiveModelBuilder(contentRetrievalService, new ArchiveLinksService(),
    new DateFormatter(DateTimeZone.UTC), new RssUrlBuilder(new SiteInformation()))

  @Test
  def isValidForArchiveMonthUrl(): Unit = {
    request.setRequestURI("/archive/2020-jul")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def archivePageMainContentIsTheArchiveMonthNewsitems(): Unit = {
    request.setRequestURI("/archive/2020-jul")
    val july = new DateTime(2020, 7, 1, 0, 0, DateTimeZone.UTC)
    val monthOfJuly = new Interval(july, july.plusMonths(1))
    when(contentRetrievalService.getNewsitemsForInterval(monthOfJuly, loggedInUser)).thenReturn(Future.successful(monthNewsitems))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(monthNewsitems.asJava, mv.get(MAIN_CONTENT))
    assertEquals("July 2020", mv.get("heading"))
    assertEquals("/rss", mv.get("rss_url"))
  }

  @Test
  def extraContentIncludesLinksToArchiveMonths(): Unit = {
    request.setRequestURI("/archive/2020-jul")
    val july = new DateTime(2020, 7, 1, 0, 0, DateTimeZone.UTC)
    val monthOfJuly = new Interval(july, july.plusMonths(1))

    val archiveLinks = Seq(ArchiveLink(monthOfJuly, Some(3)))

    when(contentRetrievalService.getArchiveMonths(None)).thenReturn(Future.successful(archiveLinks))
    when(contentRetrievalService.getArchiveTypeCounts(None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getPublishersForInterval(monthOfJuly, None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getLatestNewsitems(maxItems = 5, loggedInUser = None)).thenReturn(Future.successful(Seq.empty))

    val extras = Await.result(modelBuilder.populateExtraModelContent(request, None), TenSeconds)

    assertEquals(archiveLinks.asJava, extras.get("archive_links"))
  }

  @Test
  def shouldIncludePreviousAndNextMonthLinks(): Unit = {
    request.setRequestURI("/archive/2021-feb")
    val feb = new DateTime(2021, 2, 1, 0, 0, DateTimeZone.UTC)
    val monthOfFeb = new Interval(feb, feb.plusMonths(1))

    when(contentRetrievalService.getArchiveMonths(None)).thenReturn(Future.successful(someArchiveMonths))
    when(contentRetrievalService.getArchiveTypeCounts(None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getPublishersForInterval(monthOfFeb, None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getLatestNewsitems(maxItems = 5, loggedInUser = None)).thenReturn(Future.successful(Seq.empty))

    val extras = Await.result(modelBuilder.populateExtraModelContent(request, None), TenSeconds)

    assertEquals(someArchiveMonths.head, extras.get("previous_month"))
    assertEquals(someArchiveMonths.last, extras.get("next_month"))
  }

}
