package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.controllers.{LoggedInUserFilter, RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, RssfeedNewsitemService}
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.{ContentRetrievalService, TagDAO}
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.Future

class TagModelBuilderTest {

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val rssUrlBuilder = mock(classOf[RssUrlBuilder])
  private val urlBuilder = mock(classOf[UrlBuilder])
  private val relatedTagsService = mock(classOf[RelatedTagsService])
  private val rssfeedNewsitemService = mock(classOf[RssfeedNewsitemService])
  private val feedItemLocalCopyDecorator = mock(classOf[FeedItemLocalCopyDecorator])
  private val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])
  private val tagDAO = mock(classOf[TagDAO])
  private val frontendResourceMapper = mock(classOf[FrontendResourceMapper])
  private val loggedInUserFilter = mock(classOf[LoggedInUserFilter])

  private val newsitem1 = mock(classOf[FrontendResource])
  private val newsitem2 = mock(classOf[FrontendResource])

  private val TAG_DISPLAY_NAME = "Penguins"

  private val parentTag = Tag(display_name = "Parent")
  private val tag = Tag(parent = Some(parentTag._id), display_name = TAG_DISPLAY_NAME)

  private val loggedInUser = None

  val request = new MockHttpServletRequest()

  private val modelBuilder = new TagModelBuilder(rssUrlBuilder, urlBuilder, relatedTagsService, rssfeedNewsitemService,
    contentRetrievalService, feedItemLocalCopyDecorator, commonAttributesModelBuilder, tagDAO, frontendResourceMapper, loggedInUserFilter)


  @Before
  def setup {
    when(tagDAO.loadTagsByParent(tag._id)).thenReturn(Future.successful(List.empty))
    when(tagDAO.loadTagByObjectId(parentTag._id)).thenReturn(Future.successful(Some(parentTag)))
  }

  @Test
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
    when(contentRetrievalService.getTaggedNewsitems(tag, 0, 30, loggedInUser)).thenReturn(Future.successful((tagNewsitems, tagNewsitems.size.toLong)))

    val mv = modelBuilder.populateContentModel(request).get

    assertEquals(TAG_DISPLAY_NAME, mv.getModel.get("heading"))
  }

  @Test
  def mainContentShouldBeTagNewsitems {
    request.setAttribute("tags", Seq(tag))
    val tagNewsitems = Seq(newsitem1, newsitem2) // TODO populate with content; mocking breaks asJava
    when(contentRetrievalService.getTaggedNewsitems(tag, 0, 30, loggedInUser)).thenReturn(Future.successful((tagNewsitems, tagNewsitems.size.toLong)))

    val mv = modelBuilder.populateContentModel(request).get

    import scala.collection.JavaConverters._
    assertEquals(tagNewsitems.asJava, mv.getModel.get("main_content"))
  }

  @Test
  def shouldIncludeTagParent = {
    request.setAttribute("tags", Seq(tag))
    val tagNewsitems = Seq(newsitem1, newsitem2) // TODO populate with content; mocking breaks asJava
    when(contentRetrievalService.getTaggedNewsitems(tag, 0, 30, loggedInUser)).thenReturn(Future.successful((tagNewsitems, tagNewsitems.size.toLong)))

    val mv = modelBuilder.populateContentModel(request).get

    assertEquals(parentTag, mv.getModel.get("parent"))
  }

}
