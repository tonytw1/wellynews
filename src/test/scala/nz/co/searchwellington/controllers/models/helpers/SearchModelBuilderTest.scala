package nz.co.searchwellington.controllers.models.helpers

import java.util.List

import com.google.common.collect.Lists
import nz.co.searchwellington.controllers.models.SearchModelBuilder
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.{Before, Test}
import org.mockito.{Mock, Mockito, MockitoAnnotations}
import org.springframework.mock.web.MockHttpServletRequest

class SearchModelBuilderTest {
  @Mock private[helpers] val contentRetrievalService: ContentRetrievalService = null
  @Mock private[helpers] val urlBuilder: UrlBuilder = null
  @Mock private[helpers] val commonAttributesModelBuilder: CommonAttributesModelBuilder = null
  @Mock private[helpers] val tag: Tag = null
  private var tags: java.util.List[Tag] = null
  private var request: MockHttpServletRequest = null
  private var modelBuilder: SearchModelBuilder = null
  @Mock private[helpers] val tagKeywordNewsitemResults: List[FrontendResource] = null

  @Before def setup {
    MockitoAnnotations.initMocks(this)
    request = new MockHttpServletRequest
    modelBuilder = new SearchModelBuilder(contentRetrievalService, urlBuilder, commonAttributesModelBuilder)
    tags = Lists.newArrayList
    tags.add(tag)
  }

  @Test
  @throws[Exception]
  def keywordShouldBeSetToIndicateASearch {
    request.setPathInfo("")
    assertFalse(modelBuilder.isValid(request))
    request.setParameter("keywords", "widgets")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  @throws[Exception]
  def pageHeadingShouldBeSearchKeyword {
    request.setParameter("keywords", "widgets")
    assertEquals("Search results - widgets", modelBuilder.populateContentModel(request).get.getModel.get("heading"))
  }

  @Test
  @throws[Exception]
  def shouldGetTagRefinementResultsIfTagIsSet {
    request.setParameter("keywords", "widgets")
    request.setAttribute("tags", tags)
    Mockito.when(contentRetrievalService.getNewsitemsMatchingKeywords(Mockito.eq("widgets"), Mockito.eq(tag), Mockito.eq(0), Mockito.eq(30))).thenReturn(tagKeywordNewsitemResults)
    val mv = modelBuilder.populateContentModel(request).get
    assertEquals(tagKeywordNewsitemResults, mv.getModel.get("main_content"))
    assertEquals(tag, mv.getModel.get("tag"))
  }

}