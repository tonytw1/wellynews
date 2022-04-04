package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.model.{ArchiveLink, SiteInformation}
import nz.co.searchwellington.model.frontend.FrontendNewsitem
import nz.co.searchwellington.model.helpers.ArchiveLinksService
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.joda.time.{DateTime, DateTimeZone, Interval}
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import uk.co.eelpieconsulting.common.dates.DateFormatter

import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._
import scala.concurrent.ExecutionContext.Implicits.global

class ArchiveModelBuilderTest extends ReasonableWaits with ContentFields {

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val request = new MockHttpServletRequest

  private val newsitem = new FrontendNewsitem(id = "123")
  private val anotherNewsitem = FrontendNewsitem(id = "456")
  private val monthNewsitems = Seq(newsitem, anotherNewsitem)

  private val loggedInUser = None

  val modelBuilder =  new ArchiveModelBuilder(contentRetrievalService, new ArchiveLinksService(),
    new DateFormatter(DateTimeZone.UTC), new RssUrlBuilder(new SiteInformation()))

  @Before
  def setup() {
    request.setRequestURI("/archive/2020-jul")
  }

  @Test
  def isValidForArchiveMonthUrl(): Unit = {
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def archivePageMainContentIsTheArchiveMonthNewsitems(): Unit = {
    val july = new DateTime(2020, 7, 1, 0, 0, DateTimeZone.UTC)
    val monthOfJuly = new Interval(july, july.plusMonths(1))
    when(contentRetrievalService.getNewsitemsForInterval(monthOfJuly, loggedInUser)).thenReturn(Future.successful(monthNewsitems))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(monthNewsitems.asJava, mv.getModel.get(MAIN_CONTENT))
    assertEquals("July 2020", mv.getModel.get("heading"))
    assertEquals("/rss", mv.getModel.get("rss_url"))
  }

  @Test
  def extraContentIncludesLinksToArchiveMonths(): Unit = {
    val july = new DateTime(2020, 7, 1, 0, 0, DateTimeZone.UTC)
    val monthOfJuly = new Interval(july, july.plusMonths(1))

    val archiveLinks = Seq(ArchiveLink(monthOfJuly, 3))

    when(contentRetrievalService.getArchiveMonths(None)).thenReturn(Future.successful(archiveLinks))
    when(contentRetrievalService.getArchiveTypeCounts(None)).thenReturn(Future.successful(Map[String, Long]()))
    when(contentRetrievalService.getPublishersForInterval(monthOfJuly, None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getLatestNewsitems(maxItems = 5, loggedInUser = None)).thenReturn(Future.successful(Seq.empty))

    val extras = Await.result(modelBuilder.populateExtraModelContent(request, None), TenSeconds)

    assertEquals(archiveLinks.asJava, extras.get("archive_links"))
  }

  @Test
  def shouldIncludePreviousAndNextMonthLinks(): Unit = {
    val june = new DateTime(2020, 6, 1, 0, 0, DateTimeZone.UTC)
    val july = june.plusMonths(1)
    val august = july.plusMonths(1)

    val monthOfJune = new Interval(june, june.plusMonths(1))
    val monthOfJuly = new Interval(july, july.plusMonths(1))
    val monthOfAugust = new Interval(august, august.plusMonths(1))

    val archiveLinks = Seq(ArchiveLink(monthOfJune, 1), ArchiveLink(monthOfJuly, 3), ArchiveLink(monthOfAugust, 4))

    when(contentRetrievalService.getArchiveMonths(None)).thenReturn(Future.successful(archiveLinks))
    when(contentRetrievalService.getArchiveTypeCounts(None)).thenReturn(Future.successful(Map[String, Long]()))
    when(contentRetrievalService.getPublishersForInterval(monthOfJuly, None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getLatestNewsitems(maxItems = 5, loggedInUser = None)).thenReturn(Future.successful(Seq.empty))

    val extras = Await.result(modelBuilder.populateExtraModelContent(request, None), TenSeconds)

    assertEquals(ArchiveLink(monthOfJune, 1), extras.get("previous_month"))
    assertEquals(ArchiveLink(monthOfAugust, 4), extras.get("next_month"))
  }

}
