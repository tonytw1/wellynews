package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.frontend.{FrontendNewsitem, FrontendWebsite}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{PublisherContentCount, Tag, TagContentCount, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertEquals, assertFalse, assertNull, assertTrue}
import org.junit.Test
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

class SearchModelBuilderTest extends ReasonableWaits with ContentFields {
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val urlBuilder = mock(classOf[UrlBuilder])
  private val frontendResourceMapper = mock(classOf[FrontendResourceMapper])

  private val tag = Tag(id = UUID.randomUUID().toString, name = "A tag")

  private val tagNewsitem = FrontendNewsitem(id = UUID.randomUUID().toString, name = "A tag newsitem")
  private val anotherTagNewsitem = FrontendNewsitem(id = UUID.randomUUID().toString, name = "Another tag newsitem")

  private val keywordNewsitemResults = (Seq.empty, 0L)
  private val tagKeywordNewsitemResults = (Seq(tagNewsitem, anotherTagNewsitem), 2L)
  private val websitesMatchingTag = (Seq(FrontendWebsite(id = UUID.randomUUID().toString), FrontendWebsite(id = UUID.randomUUID().toString)), 2L)
  private val noLoggedInUser = None

  private val modelBuilder = new SearchModelBuilder(contentRetrievalService, urlBuilder, frontendResourceMapper)

  private val emptySearchResults = (Seq.empty, 0L)

  @Test
  def keywordShouldBeSetToIndicateASearch(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("")
    assertFalse(modelBuilder.isValid(request))

    request.setParameter("q", "widgets")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def pageHeadingShouldBeSearchKeyword(): Unit = {
    val request = new MockHttpServletRequest
    request.setParameter("q", "widgets")
    when(contentRetrievalService.getNewsitemsMatchingKeywords("widgets", 0, 30, noLoggedInUser, tag = None, publisher = None)).thenReturn(Future.successful(keywordNewsitemResults))
    when(contentRetrievalService.getNewsitemKeywordSearchRelatedTags("widgets", noLoggedInUser)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getNewsitemKeywordSearchRelatedPublishers("widgets", noLoggedInUser)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getWebsitesMatchingKeywords("widgets", None, 0, 30, noLoggedInUser)).thenReturn(Future.successful(emptySearchResults))

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
    when(contentRetrievalService.getNewsitemsMatchingKeywords(q, 0, 30, noLoggedInUser, tag = None, publisher = None)).thenReturn(Future.successful(keywordNewsitemResults))
    when(contentRetrievalService.getNewsitemKeywordSearchRelatedTags(q, noLoggedInUser)).thenReturn(Future.successful(tagRefinements))
    when(contentRetrievalService.getNewsitemKeywordSearchRelatedPublishers(q, noLoggedInUser)).thenReturn(Future.successful(publisherRefinements))
    when(contentRetrievalService.getWebsitesMatchingKeywords(q, None, 0, 30, noLoggedInUser)).thenReturn(Future.successful(emptySearchResults))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get
    assertEquals(tagRefinements.asJava, mv.getModel.get("related_tags"))
    assertEquals(publisherRefinements.asJava, mv.getModel.get("related_publishers"))
  }

  @Test
  def canSearchForSpecificPublishersNewsitems(): Unit = {
    val publisher = Website(id = "123", title = "A publisher with lots of newsitems")

    val request = new MockHttpServletRequest
    request.setParameter("q", "sausages")
    request.setAttribute("publisher", publisher)

    val publisherNewsitemSearchResults = (Seq(tagNewsitem, anotherTagNewsitem), 2L)

    when(contentRetrievalService.getNewsitemsMatchingKeywords("sausages", 0, 30, noLoggedInUser, tag = None, publisher = Some(publisher))).
      thenReturn(Future.successful(publisherNewsitemSearchResults))
    when(frontendResourceMapper.createFrontendResourceFrom(publisher, None)).thenReturn(Future.successful(FrontendWebsite(id = "123")))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(publisherNewsitemSearchResults._1.asJava, mv.getModel.get(MAIN_CONTENT))

    // Do not show refinements when a publisher refinement has been selected
    assertNull(mv.getModel.get("related_tags"))
    assertNull(mv.getModel.get("related_publishers"))

    // Do not show publishers when a publisher is selected
    assertNull( mv.getModel.get("secondary_content"))
    assertNull(mv.getModel.get("secondary_heading"))
  }

  @Test
  def shouldShowTagIfTagFilterIsSet(): Unit = {
    val request = new MockHttpServletRequest
    request.setParameter("q", "widgets")
    request.setAttribute("tag", tag)
    when(contentRetrievalService.getNewsitemsMatchingKeywords("widgets", 0, 30, noLoggedInUser, tag = Some(tag), publisher = None)).
      thenReturn(Future.successful(tagKeywordNewsitemResults))
    when(contentRetrievalService.getNewsitemKeywordSearchRelatedPublishers("widgets", noLoggedInUser)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getNewsitemKeywordSearchRelatedTags("widgets", noLoggedInUser)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getWebsitesMatchingKeywords("widgets", Some(tag), 0, 30, noLoggedInUser)).thenReturn(Future.successful(emptySearchResults))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(tag, mv.getModel.get("tag"))
  }

  @Test
  def shouldShowTagResultsIfTagFilterIsSet(): Unit = {
    val request = new MockHttpServletRequest
    request.setParameter("q", "widgets")
    request.setAttribute("tag", tag)
    when(contentRetrievalService.getNewsitemsMatchingKeywords("widgets", 0, 30, noLoggedInUser, tag = Some(tag), publisher = None)).
      thenReturn(Future.successful(tagKeywordNewsitemResults))
    when(contentRetrievalService.getWebsitesMatchingKeywords("widgets", Some(tag), 0, 30, noLoggedInUser)).thenReturn(Future.successful(websitesMatchingTag))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(tagKeywordNewsitemResults._1.asJava, mv.getModel.get(MAIN_CONTENT))
    assertEquals(2L, mv.getModel.get("main_content_total"))

    // Do not show refinements when a publisher refinement has been selected
    assertNull(mv.getModel.get("related_tags"))
    assertNull(mv.getModel.get("related_publishers"))

    assertEquals(websitesMatchingTag._1.asJava, mv.getModel.get("secondary_content"))
  }

  @Test
  def shouldShowMatchingWebsitedAsSecondaryContent(): Unit = {
    val request = new MockHttpServletRequest
    request.setParameter("q", "widgets")

    val widgetsWebsite = FrontendWebsite(id = UUID.randomUUID().toString)
    val keywordMatchingWebsites = (Seq(widgetsWebsite), 1L)

    when(contentRetrievalService.getNewsitemsMatchingKeywords("widgets", 0, 30, noLoggedInUser, tag = None, publisher = None)).
      thenReturn(Future.successful(emptySearchResults))
    when(contentRetrievalService.getNewsitemKeywordSearchRelatedTags("widgets", noLoggedInUser)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getNewsitemKeywordSearchRelatedPublishers("widgets", noLoggedInUser)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getWebsitesMatchingKeywords("widgets", None, 0, 30, noLoggedInUser)).thenReturn(Future.successful(keywordMatchingWebsites))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(keywordMatchingWebsites._1.asJava, mv.getModel.get("secondary_content"))
    assertEquals("Matching websites", mv.getModel.get("secondary_heading"))
  }

}
