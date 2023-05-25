package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.frontend.{FrontendNewsitem, FrontendWebsite}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{SiteInformation, Tag, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.tagging.RelatedTagsService
import nz.co.searchwellington.urls.{RssUrlBuilder, UrlBuilder}
import org.junit.jupiter.api.Assertions.{assertEquals, assertNotNull, assertTrue}
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

class PublisherTagCombinerModelBuilderTest extends ReasonableWaits with ContentFields {

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val rssUrlBuilder = new RssUrlBuilder(new SiteInformation())
  private val urlBuilder = mock(classOf[UrlBuilder])
  private val relatedTagsService = mock(classOf[RelatedTagsService])
  private val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])
  private val frontendResourceMapper = mock(classOf[FrontendResourceMapper])

  private val apublisher = Website(title = "A publisher", url_words = Some("a-publisher"))
  private val atag = Tag(name = "atag", display_name = "A tag")

  private implicit val currentSpan: Span = Span.current()

  private val modelBuilder = new PublisherTagCombinerModelBuilder(contentRetrievalService, rssUrlBuilder, urlBuilder,
    relatedTagsService, commonAttributesModelBuilder, frontendResourceMapper)

  val request = new MockHttpServletRequest

  @Test
  def shouldBeValidForPublisherAndTags(): Unit = {
    request.setAttribute("publisher", apublisher)
    request.setAttribute("tag", atag)

    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def mainContentIsNewsitemsWithPublisherAndTag(): Unit = {
    request.setAttribute("publisher", apublisher)
    request.setAttribute("tag", atag)
    when(frontendResourceMapper.createFrontendResourceFrom(apublisher)).thenReturn(Future.successful(FrontendWebsite(id = "123", name = "A publisher")))

    val expectedNewsitems = Seq(FrontendNewsitem(id = "123"), FrontendNewsitem(id = "456"))
    when(contentRetrievalService.getPublisherTagCombinerNewsitems(apublisher, atag, 0, 30, None)).thenReturn(Future.successful(expectedNewsitems))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(expectedNewsitems.asJava, mv.get(MAIN_CONTENT))
    assertNotNull(mv.get("publisher"))
    assertNotNull(mv.get("tag"))
    assertEquals("/a-publisher+atag/rss", mv.get("rss_url"))
  }

}
