package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.model.PaginationLink
import nz.co.searchwellington.model.frontend.FrontendWatchlist
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.{Await, Future}

class WatchlistModelBuilderTest extends ReasonableWaits with ContentFields {

  private val rssUrlBuilder = mock(classOf[RssUrlBuilder])
  private val urlBuilder = mock(classOf[UrlBuilder])
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])

  private val modelBuilder = new WatchlistModelBuilder(contentRetrievalService, rssUrlBuilder, urlBuilder, commonAttributesModelBuilder)

  @Test
  def mainContentShouldBeWatchlistItems() {
    val request = new MockHttpServletRequest
    val watchlistItems = (Seq(FrontendWatchlist(id = "789", lastChanged = None, lastScanned = None)), 40L)

    when(contentRetrievalService.getWatchlistItems(None, page = 1)).thenReturn(Future.successful(watchlistItems))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    import scala.collection.JavaConverters._
    assertEquals(watchlistItems._1.asJava, mv.getModel.get(MAIN_CONTENT))

    val pageLinks = mv.getModel.get("page_links").asInstanceOf[java.util.List[PaginationLink]].asScala
    assertEquals(2, pageLinks.size)
  }

}
