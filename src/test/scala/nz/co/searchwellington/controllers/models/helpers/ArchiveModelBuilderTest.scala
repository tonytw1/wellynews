package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.ArchiveLink
import nz.co.searchwellington.model.frontend.FrontendNewsitem
import nz.co.searchwellington.model.helpers.ArchiveLinksService
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.joda.time.{DateTime, Interval}
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.{Await, Future}

class ArchiveModelBuilderTest extends ReasonableWaits with ContentFields {

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val request = new MockHttpServletRequest

  private val newsitem = new FrontendNewsitem(id = "123")
  private val anotherNewsitem = FrontendNewsitem(id = "456")
  private val monthNewsitems = Seq(newsitem, anotherNewsitem)

  private val loggedInUser = None

  val modelBuilder =  new ArchiveModelBuilder(contentRetrievalService, new ArchiveLinksService())

  @Before
  def setup {
    request.setRequestURI("/archive/2020-jul")
  }

  @Test
  def isValidForArchiveMonthUrl() {
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def indexPageMainContentIsTheArchiveMonthNewsitems() {
    val july = new DateTime(2020, 7, 1, 0, 0)
    val monthOfJuly = new Interval(july, july.plusMonths(1))
    when(contentRetrievalService.getNewsitemsForInterval(monthOfJuly, loggedInUser)).thenReturn(Future.successful(monthNewsitems))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    import scala.collection.JavaConverters._
    assertEquals(monthNewsitems.asJava, mv.getModel.get(MAIN_CONTENT))
  }

  @Test
  def extraContentIncludesLinksToArchiveMonths(): Unit = {
    val mv = new ModelAndView()
    val july = new DateTime(2020, 7, 1, 0, 0)
    val monthOfJuly = new Interval(july, july.plusMonths(1))

    val archiveLinks: Seq[ArchiveLink] = Seq(ArchiveLink(monthOfJuly, 3))

    when(contentRetrievalService.getArchiveMonths(None)).thenReturn(Future.successful(archiveLinks))
    when(contentRetrievalService.getArchiveCounts(None)).thenReturn(Future.successful(Map[String, Long]()))
    when(contentRetrievalService.getPublishersForInterval(monthOfJuly, None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getLatestNewsitems(maxItems = 5, loggedInUser = None)).thenReturn(Future.successful(Seq.empty))

    val withExtras = Await.result(modelBuilder.populateExtraModelContent(request, mv, None), TenSeconds)

    import scala.collection.JavaConverters._
    assertEquals(archiveLinks.asJava, withExtras.getModel.get("archive_links"))
  }

}
