package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.model.frontend.{FrontendNewsitem, FrontendResource}
import nz.co.searchwellington.repositories.{ContentRetrievalService, TagDAO}
import nz.co.searchwellington.tagging.RelatedTagsService
import nz.co.searchwellington.urls.{RssUrlBuilder, UrlBuilder}
import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse, assertTrue}
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import reactivemongo.api.bson.BSONObjectID

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

class TagCombinerModelBuilderTest extends ReasonableWaits with ContentFields {

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val rssUrlBuilder = mock(classOf[RssUrlBuilder])
  private val urlBuilder = mock(classOf[UrlBuilder])
  private val relatedTagsService = mock(classOf[RelatedTagsService])
  private val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])
  private val tagDAO = mock(classOf[TagDAO])

  private val tag = Tag(_id = BSONObjectID.generate, id = UUID.randomUUID().toString, display_name = "Penguins")
  private val anotherTag = Tag(_id = BSONObjectID.generate, id = UUID.randomUUID().toString, display_name = "Airport")

  private val request = new MockHttpServletRequest()

  private implicit val currentSpan: Span = Span.current()

  private val modelBuilder = new TagCombinerModelBuilder(contentRetrievalService, rssUrlBuilder, urlBuilder, relatedTagsService, commonAttributesModelBuilder)

  @Test
  def isNotValidIfNotTagsAreOnTheRequest(): Unit = {
    assertFalse(modelBuilder.isValid(request))
  }

  @Test
  def isNotValidIsOneTagIsOnTheRequest(): Unit = {
    request.setAttribute("tags", Seq(tag))
    assertFalse(modelBuilder.isValid(request))
  }

  @Test
  def isValidIfTwoTagsIsOnTheRequest(): Unit = {
    request.setAttribute("tags", Seq(tag, tag))
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def mainContentIsTagCombinerNewsitems(): Unit = {
    request.setAttribute("tags", Seq(tag, anotherTag))
    val tagCombinerNewsitems = (Seq[FrontendResource](FrontendNewsitem(id = "123"), FrontendNewsitem(id = "456")), 2L)
    val tags = Seq(tag, anotherTag)
    when(contentRetrievalService.getTaggedNewsitems(tags.toSet,30, None)).thenReturn(Future.successful(tagCombinerNewsitems))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(tags.asJava, mv.get("tags"))
    assertEquals(tagCombinerNewsitems._1.asJava, mv.get(MAIN_CONTENT))
  }

}
