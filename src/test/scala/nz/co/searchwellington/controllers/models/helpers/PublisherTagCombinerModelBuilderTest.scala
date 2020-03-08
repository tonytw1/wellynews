package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.{RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.model.frontend.FrontendNewsitem
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Tag, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.{Await, Future}

class PublisherTagCombinerModelBuilderTest extends ReasonableWaits {

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val rssUrlBuilder = mock(classOf[RssUrlBuilder])
  private val urlBuilder = mock(classOf[UrlBuilder])
  private val relatedTagsService = mock(classOf[RelatedTagsService])
  private val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])
  private val frontendResourceMapper = mock(classOf[FrontendResourceMapper])

  private val apublisher = Website(title = Some("A publisher"))
  private val atag = Tag(name = "A tag")

  private val modelBuilder = new PublisherTagCombinerModelBuilder(contentRetrievalService, rssUrlBuilder, urlBuilder,
    relatedTagsService, commonAttributesModelBuilder, frontendResourceMapper)

  val request = new MockHttpServletRequest

  @Test
  def isValidForPublisherAndTags(): Unit = {
    request.setAttribute("publisher", apublisher)
    request.setAttribute("tag", atag)

    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def mainContentIsNewsitemsWithPublisherAndTag(): Unit = {
    request.setAttribute("publisher", apublisher)
    request.setAttribute("tag", atag)
    val expectedNewsitems= Seq(FrontendNewsitem(id = "123"), FrontendNewsitem(id = "456"))

    when(contentRetrievalService.getPublisherTagCombinerNewsitems(apublisher, atag, 30, None)).thenReturn(Future.successful(expectedNewsitems))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    import scala.collection.JavaConverters._
    assertEquals(expectedNewsitems.asJava, mv.getModel.get("main_content"))
  }

}
