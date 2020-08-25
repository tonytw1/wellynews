package nz.co.searchwellington.controllers.models.helpers

import java.util.UUID

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.SearchModelBuilder
import nz.co.searchwellington.model.frontend.{FrontendResource, FrontendWebsite}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{PublisherContentCount, Tag, TagContentCount, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.Test
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class SearchModelBuilderTest extends ReasonableWaits with ContentFields {
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val urlBuilder = mock(classOf[UrlBuilder])
  private val frontendResourceMapper = mock(classOf[FrontendResourceMapper])

  private val tag = Tag(id = UUID.randomUUID().toString, name = "A tag")

  private val tagNewsitem = mock(classOf[FrontendResource])
  private val anotherTagNewsitem = mock(classOf[FrontendResource])

  private val keywordNewsitemResults = (Seq.empty, 0L)
  private val tagKeywordNewsitemResults = (Seq(tagNewsitem, anotherTagNewsitem), 2L)

  private val loggedInUser = None

  private val modelBuilder = new SearchModelBuilder(contentRetrievalService, urlBuilder, frontendResourceMapper)


  @Test
  def keywordShouldBeSetToIndicateASearch() {
    val request = new MockHttpServletRequest
    request.setRequestURI("")
    assertFalse(modelBuilder.isValid(request))

    request.setParameter("q", "widgets")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def pageHeadingShouldBeSearchKeyword() {
    val request = new MockHttpServletRequest
    request.setParameter("q", "widgets")
    when(contentRetrievalService.getNewsitemsMatchingKeywords("widgets", 0, 30, loggedInUser, tag = None, publisher = None)).thenReturn(Future.successful(keywordNewsitemResults))
    when(contentRetrievalService.getNewsitemKeywordSearchRelatedTags("widgets", loggedInUser)).thenReturn(Seq.empty)
    when(contentRetrievalService.getNewsitemKeywordSearchRelatedPublishers("widgets", loggedInUser)).thenReturn(Seq.empty)

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals("Search results - widgets", mv.getModel.get("heading"))
  }

  @Test
  def shouldShowTagAndPublisherRefinements(): Unit = {
    val q = "widgets"

    val tag = Tag()
    val anotherTag = Tag()
    val tagRefinements = Seq[TagContentCount](TagContentCount(tag, 1L), TagContentCount(anotherTag, 2L))

    val publisher = Website()
    val anotherPublisher = Website()
    val publisherRefinements = Seq[PublisherContentCount](PublisherContentCount(publisher, 1L), PublisherContentCount(anotherPublisher, 2L))

    val request = new MockHttpServletRequest
    request.setParameter("q", q)
    when(contentRetrievalService.getNewsitemsMatchingKeywords(q, 0, 30, loggedInUser, tag = None, publisher = None)).thenReturn(Future.successful(keywordNewsitemResults))
    when(contentRetrievalService.getNewsitemKeywordSearchRelatedTags(q, loggedInUser)).thenReturn(tagRefinements)
    when(contentRetrievalService.getNewsitemKeywordSearchRelatedPublishers(q, loggedInUser)).thenReturn(publisherRefinements)

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    import scala.collection.JavaConverters._
    assertEquals(tagRefinements.asJava, mv.getModel.get("related_tags"))
    assertEquals(publisherRefinements.asJava, mv.getModel.get("related_publishers"))
  }

  @Test
  def canSearchSpecificPublishersNewsitems() = {
    val publisher = Website(id = "123", title = Some("A publisher with lots of newsitems"))

    val request = new MockHttpServletRequest
    request.setParameter("q", "sausages")
    request.setAttribute("publisher", publisher)

    val publisherNewsitemSearchResults = (Seq(tagNewsitem, anotherTagNewsitem), 2L)
    when(contentRetrievalService.getNewsitemsMatchingKeywords("sausages", 0, 30, loggedInUser, tag = None, publisher = Some(publisher))).
      thenReturn(Future.successful(publisherNewsitemSearchResults))
    when(contentRetrievalService.getNewsitemKeywordSearchRelatedTags("sausages", loggedInUser)).thenReturn(Seq.empty)
    when(contentRetrievalService.getNewsitemKeywordSearchRelatedPublishers("sausages", loggedInUser)).thenReturn(Seq.empty)
    when(frontendResourceMapper.createFrontendResourceFrom(publisher, None)).thenReturn(Future.successful(FrontendWebsite(id = "123")))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    import scala.collection.JavaConverters._
    assertEquals(publisherNewsitemSearchResults._1.asJava, mv.getModel.get(MAIN_CONTENT))
  }

  @Test
  def shouldShowTagIfTagFilterIsSet() {
    val request = new MockHttpServletRequest
    request.setParameter("q", "widgets")
    request.setAttribute("tag", tag)
    when(contentRetrievalService.getNewsitemsMatchingKeywords("widgets", 0, 30, loggedInUser, tag = Some(tag), publisher = None)).
      thenReturn(Future.successful(tagKeywordNewsitemResults))
    when(contentRetrievalService.getNewsitemKeywordSearchRelatedPublishers("widgets", loggedInUser)).thenReturn(Seq.empty)
    when(contentRetrievalService.getNewsitemKeywordSearchRelatedTags("widgets", loggedInUser)).thenReturn(Seq.empty)

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(tag, mv.getModel.get("tag"))
  }

  @Test
  def shouldShowTagResultsIfTagFilterIsSet() {
    val request = new MockHttpServletRequest
    request.setParameter("q", "widgets")
    request.setAttribute("tag", tag)
    when(contentRetrievalService.getNewsitemsMatchingKeywords("widgets", 0, 30, loggedInUser, tag = Some(tag), publisher = None)).
      thenReturn(Future.successful(tagKeywordNewsitemResults))
    when(contentRetrievalService.getNewsitemKeywordSearchRelatedTags("widgets", loggedInUser)).thenReturn(Seq.empty)
    when(contentRetrievalService.getNewsitemKeywordSearchRelatedPublishers("widgets", loggedInUser)).thenReturn(Seq.empty)

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    import scala.collection.JavaConverters._
    assertEquals(tagKeywordNewsitemResults._1.asJava, mv.getModel.get(MAIN_CONTENT))
    assertEquals(2L, mv.getModel.get("main_content_total"))
  }

}
