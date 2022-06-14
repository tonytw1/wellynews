package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.helpers.ArchiveLinksService
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
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

  private val newsitem = mock(classOf[FrontendResource])
  private val anotherNewsitem = mock(classOf[FrontendResource])
  private val latestNewsitems = Seq(newsitem, anotherNewsitem)

  private val loggedInUser = None

  val modelBuilder =  new IndexModelBuilder(contentRetrievalService, rssUrlBuilder, urlBuilder, archiveLinksService, commonAttributesModelBuilder)

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
    when(contentRetrievalService.getLatestNewsitems(30, 1, loggedInUser)).thenReturn(Future.successful(latestNewsitems))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(latestNewsitems.asJava, mv.getModel.get(MAIN_CONTENT))
  }

}
