package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.frontend.FrontendNewsitem
import nz.co.searchwellington.model.helpers.ArchiveLinksService
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.{RssUrlBuilder, UrlBuilder}
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

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

  private val newsitem = FrontendNewsitem(id = "123", name = "Newsitem title", date =Some(DateTime.now.toDate))
  private val anotherNewsitem = FrontendNewsitem(id = "456", name = "Newsitem title", date =Some(DateTime.now.toDate))
  private val latestNewsitems = Seq(newsitem, anotherNewsitem)

  private val loggedInUser = None

  private implicit val currentSpan: Span = Span.current()

  private val modelBuilder =  new IndexModelBuilder(contentRetrievalService, rssUrlBuilder, urlBuilder, archiveLinksService, commonAttributesModelBuilder)

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

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(latestNewsitems.asJava, mv.get(MAIN_CONTENT))
  }

}
