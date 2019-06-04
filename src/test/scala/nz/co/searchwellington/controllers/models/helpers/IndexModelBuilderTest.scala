package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.controllers.{LoggedInUserFilter, RssUrlBuilder}
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.helpers.ArchiveLinksService
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.Future

class IndexModelBuilderTest {

  val contentRetrievalService = mock(classOf[ContentRetrievalService])
  val rssUrlBuilder = mock(classOf[RssUrlBuilder])
  val loggedInUserFilter = mock(classOf[LoggedInUserFilter])
  val urlBuilder = mock(classOf[UrlBuilder])
  val archiveLinksService = mock(classOf[ArchiveLinksService])
  val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])

  val request = new MockHttpServletRequest

  val newsitem = org.mockito.Mockito.mock(classOf[FrontendResource])
  val anotherNewsitem = org.mockito.Mockito.mock(classOf[FrontendResource])
  val latestNewsitems = Seq(newsitem, anotherNewsitem)

  val modelBuilder =  new IndexModelBuilder(contentRetrievalService, rssUrlBuilder, loggedInUserFilter, urlBuilder, archiveLinksService, commonAttributesModelBuilder)

  @Before def setup {
    request.setPathInfo("/")
  }

  @Test
  @throws[Exception]
  def isValidForHomePageUrl {
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  @throws[Exception]
  def isNotValidForMainRssUrlAsThatsTakenCareOfByFeedBurner {
    request.setPathInfo("/rss")
    assertFalse(modelBuilder.isValid(request))
  }

  @Test
  @throws[Exception]
  def isValidForMainJsonUrl {
    request.setPathInfo("/json")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  @throws[Exception]
  def indexPageMainContentIsTheLatestNewsitems {
    when(contentRetrievalService.getLatestNewsitems(30, 1)).thenReturn(Future.successful(latestNewsitems))

    val mv = modelBuilder.populateContentModel(request).get

    import scala.collection.JavaConverters._
    assertEquals(latestNewsitems.asJava, mv.getModel.get("main_content"))
  }

}
