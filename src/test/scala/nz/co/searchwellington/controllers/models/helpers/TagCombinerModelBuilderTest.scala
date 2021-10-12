package nz.co.searchwellington.controllers.models.helpers

import java.util.UUID
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.model.frontend.{FrontendNewsitem, FrontendResource}
import nz.co.searchwellington.repositories.{ContentRetrievalService, TagDAO}
import nz.co.searchwellington.tagging.RelatedTagsService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.{Await, Future}

class TagCombinerModelBuilderTest extends ReasonableWaits with ContentFields {

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val rssUrlBuilder = mock(classOf[RssUrlBuilder])
  private val urlBuilder = mock(classOf[UrlBuilder])
  private val relatedTagsService = mock(classOf[RelatedTagsService])
  private val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])
  private val tagDAO = mock(classOf[TagDAO])

  private val tag = Tag(_id = BSONObjectID.generate, id = UUID.randomUUID().toString, display_name = "Penguins")
  private val anotherTag = Tag(_id = BSONObjectID.generate, id = UUID.randomUUID().toString, display_name = "Airport")

  val request = new MockHttpServletRequest()

  private val modelBuilder = new TagCombinerModelBuilder(contentRetrievalService, rssUrlBuilder, urlBuilder, relatedTagsService, commonAttributesModelBuilder)

  @Before
  def setup {
  }

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
    when(contentRetrievalService.getTaggedNewsitems(tags.toSet, 0, 30, None)).thenReturn(Future.successful(tagCombinerNewsitems))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    import scala.collection.JavaConverters._
    assertEquals(tags.asJava, mv.getModel.get("tags"))
    assertEquals(tagCombinerNewsitems._1.asJava, mv.getModel.get(MAIN_CONTENT))
  }

}
