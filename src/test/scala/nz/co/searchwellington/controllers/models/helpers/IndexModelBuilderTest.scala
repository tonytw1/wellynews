package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RssUrlBuilder}
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.helpers.ArchiveLinksService
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.{Await, Future}

class IndexModelBuilderTest extends ReasonableWaits {

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val rssUrlBuilder = mock(classOf[RssUrlBuilder])
  private val urlBuilder = mock(classOf[UrlBuilder])
  private val archiveLinksService = mock(classOf[ArchiveLinksService])
  private val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])

  val request = new MockHttpServletRequest

  private val newsitem = mock(classOf[FrontendResource])
  private val anotherNewsitem = mock(classOf[FrontendResource])
  private val latestNewsitems = Seq(newsitem, anotherNewsitem)

  private val loggedInUser = None

  val modelBuilder =  new IndexModelBuilder(contentRetrievalService, rssUrlBuilder, urlBuilder, archiveLinksService, commonAttributesModelBuilder)

  @Before
  def setup {
    request.setPathInfo("/")
  }

  @Test
  def isValidForHomePageUrl {
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def isValidForMainRssUrl {
    request.setPathInfo("/rss")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def isValidForMainJsonUrl {
    request.setPathInfo("/json")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def indexPageMainContentIsTheLatestNewsitems {
    when(contentRetrievalService.getLatestNewsitems(90, 1, loggedInUser)).thenReturn(Future.successful(latestNewsitems))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    import scala.collection.JavaConverters._
    assertEquals(latestNewsitems.asJava, mv.getModel.get("main_content"))
  }

}
