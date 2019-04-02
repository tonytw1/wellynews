package nz.co.searchwellington.controllers.models.helpers

import java.util.UUID

import nz.co.searchwellington.controllers.{RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, RssfeedNewsitemService}
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.{ContentRetrievalService, TagDAO}
import nz.co.searchwellington.urls.UrlBuilder
import nz.co.searchwellington.views.GeocodeToPlaceMapper
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import reactivemongo.bson.BSONObjectID

class TagModelBuilderTest {

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val rssUrlBuilder = mock(classOf[RssUrlBuilder])
  private val urlBuilder = mock(classOf[UrlBuilder])
  private val relatedTagsService = mock(classOf[RelatedTagsService])
  private val rssfeedNewsitemService = mock(classOf[RssfeedNewsitemService])
  private val feedItemLocalCopyDecorator = mock(classOf[FeedItemLocalCopyDecorator])
  private val geocodeToPlaceMapper = mock(classOf[GeocodeToPlaceMapper])
  private val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])
  private val tagDAO = mock(classOf[TagDAO])
  private val frontendResourceMapper = mock(classOf[FrontendResourceMapper])

  private val newsitem1 = mock(classOf[FrontendResource])
  private val newsitem2 = mock(classOf[FrontendResource])

  private val TAG_DISPLAY_NAME = "Penguins"

  private val parentId = BSONObjectID.generate
  private val tag = Tag(_id = parentId, id = UUID.randomUUID().toString, display_name = TAG_DISPLAY_NAME)

  val request = new MockHttpServletRequest()

  private val modelBuilder = new TagModelBuilder(rssUrlBuilder, urlBuilder, relatedTagsService, rssfeedNewsitemService,
    contentRetrievalService, feedItemLocalCopyDecorator, geocodeToPlaceMapper, commonAttributesModelBuilder, tagDAO, frontendResourceMapper)

  @Before
  def setup {
    when(tagDAO.loadTagsByParent(parentId)).thenReturn(Seq())
  }

  @Test
  @throws(classOf[Exception])
  def isNotValidIfNotTagsAreOnTheRequest {
    assertFalse(modelBuilder.isValid(request))
  }

  @Test
  def isValidIsOneTagIsOnTheRequest {
    request.setAttribute("tags", Seq(tag))
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def isNotValidIfMoreThanOneTagIsOnTheRequest {
    request.setAttribute("tags", Seq(tag, tag))
    assertFalse(modelBuilder.isValid(request))
  }

  @Test
  def tagPageHeadingShouldBeTheTagDisplayName {
    request.setAttribute("tags", Seq(tag))
    val tagNewsitems = Seq(newsitem1, newsitem2) // TODO populate with content; mocking breaks asJava
    when(contentRetrievalService.getTaggedNewsitems(tag, 0, 30)).thenReturn(tagNewsitems)

    val mv = modelBuilder.populateContentModel(request).get

    assertEquals(TAG_DISPLAY_NAME, mv.getModel.get("heading"))
  }

  @Test
  def mainContentShouldBeTagNewsitems {
    request.setAttribute("tags", Seq(tag))
    val tagNewsitems = Seq(newsitem1, newsitem2) // TODO populate with content; mocking breaks asJava
    when(contentRetrievalService.getTaggedNewsitems(tag, 0, 30)).thenReturn(tagNewsitems)

    val mv = modelBuilder.populateContentModel(request).get

    import scala.collection.JavaConverters._
    assertEquals(tagNewsitems.asJava, mv.getModel.get("main_content"))
  }

}
