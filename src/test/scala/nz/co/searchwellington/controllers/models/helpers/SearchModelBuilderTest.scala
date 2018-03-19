package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.controllers.models.SearchModelBuilder
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

class SearchModelBuilderTest {
  val contentRetrievalService = mock(classOf[ContentRetrievalService])
  val urlBuilder = mock(classOf[UrlBuilder])
  val tag = Tag(name = "A tag")
  val tags = Seq(tag)
  private var request: MockHttpServletRequest = null
  private var modelBuilder: SearchModelBuilder = null

  val tagNewsitem: FrontendResource = org.mockito.Mockito.mock(classOf[FrontendResource])
  val tagKeywordNewsitemResults: Seq[FrontendResource] = Seq(tagNewsitem)

  @Before def setup {
    request = new MockHttpServletRequest
    modelBuilder = new SearchModelBuilder(contentRetrievalService, urlBuilder)
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

    when(contentRetrievalService.getTagNewsitemsMatchingKeywords("widgets", tag, 0, 30)).thenReturn(tagKeywordNewsitemResults)

    val mv = modelBuilder.populateContentModel(request).get

    assertEquals(tagKeywordNewsitemResults, mv.getModel.get("main_content"))
  }

}
