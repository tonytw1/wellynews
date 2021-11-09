package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.ArchiveLink
import nz.co.searchwellington.model.frontend.FrontendNewsitem
import nz.co.searchwellington.model.helpers.ArchiveLinksService
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.joda.time.{DateTime, DateTimeZone, Interval}
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.dates.DateFormatter

import scala.concurrent.{Await, Future}

class ArchiveModelBuilderTest extends ReasonableWaits with ContentFields {

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val request = new MockHttpServletRequest

  private val newsitem = new FrontendNewsitem(id = "123")
  private val anotherNewsitem = FrontendNewsitem(id = "456")
  private val monthNewsitems = Seq(newsitem, anotherNewsitem)

  private val loggedInUser = None

  val modelBuilder =  new ArchiveModelBuilder(contentRetrievalService, new ArchiveLinksService(), new DateFormatter(DateTimeZone.UTC))

  @Before
  def setup() {
    request.setRequestURI("/archive/2020-jul")
  }

  @Test
  def isValidForArchiveMonthUrl(): Unit = {
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def indexPageMainContentIsTheArchiveMonthNewsitems(): Unit = {
    val july = new DateTime(2020, 7, 1, 0, 0, DateTimeZone.UTC)
    val monthOfJuly = new Interval(july, july.plusMonths(1))
    when(contentRetrievalService.getNewsitemsForInterval(monthOfJuly, loggedInUser)).thenReturn(Future.successful(monthNewsitems))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    import scala.collection.JavaConverters._
    assertEquals(monthNewsitems.asJava, mv.getModel.get(MAIN_CONTENT))
  }

  @Test
  def extraContentIncludesLinksToArchiveMonths(): Unit = {
    val july = new DateTime(2020, 7, 1, 0, 0, DateTimeZone.UTC)
    val monthOfJuly = new Interval(july, july.plusMonths(1))

    val archiveLinks = Seq(ArchiveLink(monthOfJuly, 3))

    when(contentRetrievalService.getArchiveMonths(None)).thenReturn(Future.successful(archiveLinks))
    when(contentRetrievalService.getArchiveCounts(None)).thenReturn(Future.successful(Map[String, Long]()))
    when(contentRetrievalService.getPublishersForInterval(monthOfJuly, None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getLatestNewsitems(maxItems = 5, loggedInUser = None)).thenReturn(Future.successful(Seq.empty))

    val mv = new ModelAndView()
    val withExtras = Await.result(modelBuilder.populateExtraModelContent(request, mv, None), TenSeconds)

    import scala.collection.JavaConverters._
    assertEquals(archiveLinks.asJava, withExtras.getModel.get("archive_links"))
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
    when(contentRetrievalService.getArchiveCounts(None)).thenReturn(Future.successful(Map[String, Long]()))
    when(contentRetrievalService.getPublishersForInterval(monthOfJuly, None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getLatestNewsitems(maxItems = 5, loggedInUser = None)).thenReturn(Future.successful(Seq.empty))

    val mv = new ModelAndView()
    val withExtras = Await.result(modelBuilder.populateExtraModelContent(request, mv, None), TenSeconds)

    assertEquals(ArchiveLink(monthOfJune, 1), withExtras.getModel.get("previous_month"))
    assertEquals(ArchiveLink(monthOfAugust, 4), withExtras.getModel.get("next_month"))
  }

}
