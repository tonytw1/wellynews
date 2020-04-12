package nz.co.searchwellington.controllers.models.helpers

import java.util.UUID

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.SearchModelBuilder
import nz.co.searchwellington.model.frontend.{FrontendResource, FrontendWebsite}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Tag, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class SearchModelBuilderTest extends ReasonableWaits with ContentFields {
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val urlBuilder = mock(classOf[UrlBuilder])
  private val frontendResourceMapper = mock(classOf[FrontendResourceMapper])

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
    modelBuilder = new SearchModelBuilder(contentRetrievalService, urlBuilder, frontendResourceMapper)
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
    request.setParameter("keywords", "widgets")
    when(contentRetrievalService.getNewsitemsMatchingKeywords("widgets", 0, 30, loggedInUser, tag = None, publisher = None)).thenReturn(Future.successful(keywordNewsitemResults))
    when(contentRetrievalService.getKeywordSearchFacets("widgets")).thenReturn(Seq.empty)

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals("Search results - widgets", mv.getModel.get("heading"))
  }

  @Test
  def canSearchSpecificPublishersNewsitems(): Unit = {
    val publisher = Website(id = "123", title = Some("A publisher with lots of newsitems"))
    request.setParameter("keywords", "sausages")
    request.setAttribute("publisher", publisher)

    val publisherNewsitemSearchResults = (Seq(tagNewsitem, anotherTagNewsitem), 2L)
    when(contentRetrievalService.getNewsitemsMatchingKeywords("sausages", 0, 30, loggedInUser, tag = None, publisher = Some(publisher))).
      thenReturn(Future.successful(publisherNewsitemSearchResults))
    when(contentRetrievalService.getKeywordSearchFacets("sausages")).thenReturn(Seq.empty)
    when(frontendResourceMapper.createFrontendResourceFrom(publisher)).thenReturn(Future.successful(FrontendWebsite(id = "123")))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    import scala.collection.JavaConverters._
    assertEquals(publisherNewsitemSearchResults._1.asJava, mv.getModel.get(MAIN_CONTENT))
  }

  @Test
  def shouldShowTagIfTagFilterIsSet() {
    request.setParameter("keywords", "widgets")
    request.setAttribute("tags", tags)
    when(contentRetrievalService.getNewsitemsMatchingKeywords("widgets", 0, 30, loggedInUser, tag = Some(tag), publisher = None)).
      thenReturn(Future.successful(tagKeywordNewsitemResults))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(tag, mv.getModel.get("tag"))
  }

  @Test
  def shouldShowTagResultsIfTagFilterIsSet() {
    request.setParameter("keywords", "widgets")
    request.setAttribute("tags", tags)
    when(contentRetrievalService.getNewsitemsMatchingKeywords("widgets", 0, 30, loggedInUser, tag = Some(tag), publisher = None)).
      thenReturn(Future.successful(tagKeywordNewsitemResults))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    import scala.collection.JavaConverters._
    assertEquals(tagKeywordNewsitemResults._1.asJava, mv.getModel.get(MAIN_CONTENT))
    assertEquals(2L, mv.getModel.get("main_content_total"))
  }

}
