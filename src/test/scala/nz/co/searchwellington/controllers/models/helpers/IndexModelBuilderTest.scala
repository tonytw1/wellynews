package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.controllers.{LoggedInUserFilter, RssUrlBuilder}
import nz.co.searchwellington.model.frontend.{FrontendNewsitem, FrontendResource}
import nz.co.searchwellington.model.helpers.ArchiveLinksService
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.{Before, Test}
import org.mockito.{Mock, Mockito, MockitoAnnotations}
import org.springframework.mock.web.MockHttpServletRequest

class IndexModelBuilderTest {
  @Mock private[helpers] val contentRetrievalService: ContentRetrievalService = null
  @Mock private[helpers] val rssUrlBuilder: RssUrlBuilder = null
  @Mock private[helpers] val loggedInUserFilter: LoggedInUserFilter = null
  @Mock private[helpers] val urlBuilder: UrlBuilder = null
  @Mock private[helpers] val archiveLinksService: ArchiveLinksService = null
  @Mock private[helpers] val commonAttributesModelBuilder: CommonAttributesModelBuilder = null
  private[helpers] var request: MockHttpServletRequest = null
  var latestNewsitems: Seq[FrontendResource] = null
  private var modelBuilder: IndexModelBuilder = null

  @Before def setup {
    MockitoAnnotations.initMocks(this)
    modelBuilder = new IndexModelBuilder(contentRetrievalService, rssUrlBuilder, loggedInUserFilter, urlBuilder, archiveLinksService, commonAttributesModelBuilder)
    request = new MockHttpServletRequest
    request.setPathInfo("/")
    latestNewsitems = Seq()
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
    Mockito.when(contentRetrievalService.getLatestNewsitems(30, 1)).thenReturn(latestNewsitems)
    val mv = modelBuilder.populateContentModel(request).get
    assertEquals(latestNewsitems, mv.getModel.get("main_content"))
  }
}
