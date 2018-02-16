package nz.co.searchwellington.controllers.models.helpers

import java.util.Arrays

import nz.co.searchwellington.controllers.{RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, RssfeedNewsitemService}
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import nz.co.searchwellington.views.GeocodeToPlaceMapper
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.{Before, Test}
import org.mockito.{Mock, Mockito, MockitoAnnotations}
import org.springframework.mock.web.MockHttpServletRequest

object TagModelBuilderTest {
  private val TAG_DISPLAY_NAME: String = "Penguins"
}

class TagModelBuilderTest {
  @Mock var contentRetrievalService: ContentRetrievalService = null
  @Mock var rssUrlBuilder: RssUrlBuilder = null
  @Mock var urlBuilder: UrlBuilder = null
  @Mock var relatedTagsService: RelatedTagsService = null
  @Mock var rssfeedNewsitemService: RssfeedNewsitemService = null
  @Mock var feedItemLocalCopyDecorator: FeedItemLocalCopyDecorator = null
  @Mock var geocodeToPlaceMapper: GeocodeToPlaceMapper = null
  @Mock var commonAttributesModelBuilder: CommonAttributesModelBuilder = null
  @Mock var tagNewsitems: Seq[FrontendResource] = null
  @Mock var tag: Tag = null

  var request: MockHttpServletRequest = null
  private var modelBuilder: TagModelBuilder = null

  @Before def setup {
    MockitoAnnotations.initMocks(this)
    modelBuilder = new TagModelBuilder(rssUrlBuilder, urlBuilder, relatedTagsService, rssfeedNewsitemService, contentRetrievalService, feedItemLocalCopyDecorator, geocodeToPlaceMapper, commonAttributesModelBuilder)
    request = new MockHttpServletRequest
    Mockito.when(tag.getDisplayName).thenReturn(TagModelBuilderTest.TAG_DISPLAY_NAME)
  }

  @Test
  @throws(classOf[Exception])
  def isNotValidIfNotTagsAreOnTheRequest {
    assertFalse(modelBuilder.isValid(request))
  }

  @Test
  @throws(classOf[Exception])
  def isValidIsOneTagIsOnTheRequest {
    request.setAttribute("tags", Seq(tag))
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  @throws(classOf[Exception])
  def isNotValidIfMoreThanOneTagIsOnTheRequest {
    request.setAttribute("tags", Seq(tag, tag))
    assertFalse(modelBuilder.isValid(request))
  }

  @Test
  @throws(classOf[Exception])
  def tagPageHeadingShouldBeTheTagDisplayName {
    request.setAttribute("tags", Seq(tag))
    Mockito.when(contentRetrievalService.getTaggedNewsitems(tag, 0, 30)).thenReturn(tagNewsitems)

    val mv = modelBuilder.populateContentModel(request).get

    assertEquals(TagModelBuilderTest.TAG_DISPLAY_NAME, mv.getModel.get("heading"))
  }

  @Test
  @throws(classOf[Exception])
  def mainContentShouldBeTagNewsitems {
    request.setAttribute("tags", Seq(tag))
    Mockito.when(contentRetrievalService.getTaggedNewsitems(tag, 0, 30)).thenReturn(tagNewsitems)

    val mv = modelBuilder.populateContentModel(request).get

    assertEquals(tagNewsitems, mv.getModel.get("main_content"))
  }
}
