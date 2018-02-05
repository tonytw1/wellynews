package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.controllers.models.SearchModelBuilder
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.when
import org.mockito.{Mock, MockitoAnnotations}
import org.springframework.mock.web.MockHttpServletRequest


class SearchModelBuilderTest {
  @Mock val contentRetrievalService: ContentRetrievalService = null
  @Mock val urlBuilder: UrlBuilder = null
  @Mock val commonAttributesModelBuilder: CommonAttributesModelBuilder = null
  val tag: Tag = Tag(name = "A tag")
  private var tags: Seq[Tag] = null
  private var request: MockHttpServletRequest = null
  private var modelBuilder: SearchModelBuilder = null
  @Mock val tagKeywordNewsitemResults: Seq[FrontendResource] = null

  @Before def setup {
    MockitoAnnotations.initMocks(this)
    request = new MockHttpServletRequest
    modelBuilder = new SearchModelBuilder(contentRetrievalService, urlBuilder, commonAttributesModelBuilder)
    tags = Seq(tag)
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
  def shouldShowTagIfTagFilterIsSet {
    request.setParameter("keywords", "widgets")
    request.setAttribute("tags", tags)

    val mv = modelBuilder.populateContentModel(request).get

    assertEquals(tag, mv.getModel.get("tag"))
  }

  @Test
  @throws[Exception]
  def shouldShowTagResultsIfTagFilterIsSet {
    request.setParameter("keywords", "widgets")
    request.setAttribute("tags", tags)
    when(contentRetrievalService.getNewsitemsMatchingKeywords("widgets", tag, 0, 30)).thenReturn(tagKeywordNewsitemResults)

    val mv = modelBuilder.populateContentModel(request).get

    assertEquals(tagKeywordNewsitemResults, mv.getModel.get("main_content"))
  }

}
