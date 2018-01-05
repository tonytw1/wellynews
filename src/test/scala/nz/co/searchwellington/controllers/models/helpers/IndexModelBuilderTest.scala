package nz.co.searchwellington.controllers.models.helpers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import java.util.List
import com.google.common.collect.Lists
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.controllers.models.helpers.ArchiveLinksService
import nz.co.searchwellington.controllers.models.helpers.CommonAttributesModelBuilder
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.servlet.ModelAndView

class IndexModelBuilderTest {
  @Mock private[helpers] val contentRetrievalService: ContentRetrievalService = null
  @Mock private[helpers] val rssUrlBuilder: RssUrlBuilder = null
  @Mock private[helpers] val loggedInUserFilter: LoggedInUserFilter = null
  @Mock private[helpers] val urlBuilder: UrlBuilder = null
  @Mock private[helpers] val archiveLinksService: ArchiveLinksService = null
  @Mock private[helpers] val commonAttributesModelBuilder: CommonAttributesModelBuilder = null
  private[helpers] var request: MockHttpServletRequest = null
  private[helpers] var latestNewsitems: List[FrontendResource] = null
  private var modelBuilder: IndexModelBuilder = null

  @Before def setup {
    MockitoAnnotations.initMocks(this)
    modelBuilder = new IndexModelBuilder(contentRetrievalService, rssUrlBuilder, loggedInUserFilter, urlBuilder, archiveLinksService, commonAttributesModelBuilder)
    request = new MockHttpServletRequest
    request.setPathInfo("/")
    latestNewsitems = Lists.newArrayList(new FrontendResource)
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