package nz.co.searchwellington.controllers.models.helpers

import java.util.UUID

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.controllers.models.SearchModelBuilder
import nz.co.searchwellington.model.{Tag, Website}
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.{Await, Future}

class SearchModelBuilderTest extends ReasonableWaits {
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val urlBuilder = mock(classOf[UrlBuilder])

  private val tag = Tag(id = UUID.randomUUID().toString, name = "A tag")
  private val tags = Seq(tag)

  private var request: MockHttpServletRequest = _
  private var modelBuilder: SearchModelBuilder = _

  private val tagNewsitem = mock(classOf[FrontendResource])
  private val anotherTagNewsitem = mock(classOf[FrontendResource])

  private val keywordNewsitemResults = (Seq.empty, 0L)
  private val tagKeywordNewsitemResults = (Seq(tagNewsitem, anotherTagNewsitem), 2L)

  private val loggedInUser = None

  @Before def setup() {
    request = new MockHttpServletRequest
    modelBuilder = new SearchModelBuilder(contentRetrievalService, urlBuilder)
  }

  @Test
  def keywordShouldBeSetToIndicateASearch() {
    request.setPathInfo("")
    assertFalse(modelBuilder.isValid(request))

    request.setParameter("keywords", "widgets")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def pageHeadingShouldBeSearchKeyword() {
    when(contentRetrievalService.getNewsitemsMatchingKeywords("widgets", 0, 30, loggedInUser)).
      thenReturn(Future.successful(keywordNewsitemResults))
    request.setParameter("keywords", "widgets")

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals("Search results - widgets", mv.getModel.get("heading"))
  }

  @Test
  def canSearchSpecificPublishersNewsitems(): Unit = {
    request.setParameter("keywords", "sausages")
    request.setAttribute("publisher", new Website(id = "123", title = Some("A publisher with lots of newsitems")))

    val publisherNewsitemSearchResults = (Seq(tagNewsitem, anotherTagNewsitem), 2L)
    when(contentRetrievalService.getTagNewsitemsMatchingKeywords("widgets", tag, 0, 30, loggedInUser)).
      thenReturn(Future.successful(publisherNewsitemSearchResults))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    import scala.collection.JavaConverters._
    assertEquals(publisherNewsitemSearchResults._1.asJava, mv.getModel.get("main_content"))
  }

  @Test
  def shouldShowTagIfTagFilterIsSet() {
    request.setParameter("keywords", "widgets")
    request.setAttribute("tags", tags)
    when(contentRetrievalService.getTagNewsitemsMatchingKeywords("widgets", tag, 0, 30, loggedInUser)).
      thenReturn(Future.successful(tagKeywordNewsitemResults))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(tag, mv.getModel.get("tag"))
  }

  @Test
  def shouldShowTagResultsIfTagFilterIsSet() {
    request.setParameter("keywords", "widgets")
    request.setAttribute("tags", tags)
    when(contentRetrievalService.getTagNewsitemsMatchingKeywords("widgets", tag, 0, 30, loggedInUser)).
      thenReturn(Future.successful(tagKeywordNewsitemResults))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    import scala.collection.JavaConverters._
    assertEquals(tagKeywordNewsitemResults._1.asJava, mv.getModel.get("main_content"))
    assertEquals(2L, mv.getModel.get("main_content_total"))
  }

}
