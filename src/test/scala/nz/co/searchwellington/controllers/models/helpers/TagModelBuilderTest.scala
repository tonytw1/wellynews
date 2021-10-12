package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.feeds.{FeedItemActionDecorator, RssfeedNewsitemService}
import nz.co.searchwellington.model.frontend.{FrontendNewsitem, FrontendResource}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Geocode, SiteInformation, Tag, UrlWordsGenerator}
import nz.co.searchwellington.repositories.{ContentRetrievalService, TagDAO}
import nz.co.searchwellington.tagging.RelatedTagsService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.{Await, Future}

class TagModelBuilderTest extends ReasonableWaits with ContentFields {

  private val siteInformation = new SiteInformation("", "", "", "", "")
  private val urlBuilder = new UrlBuilder(siteInformation, new UrlWordsGenerator)
  private val rssUrlBuilder = new RssUrlBuilder(siteInformation)

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val relatedTagsService = mock(classOf[RelatedTagsService])
  private val rssfeedNewsitemService = mock(classOf[RssfeedNewsitemService])
  private val feedItemActionDecorator = mock(classOf[FeedItemActionDecorator])
  private val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])
  private val tagDAO = mock(classOf[TagDAO])
  private val frontendResourceMapper = mock(classOf[FrontendResourceMapper])

  private val newsitem1 = mock(classOf[FrontendResource])
  private val newsitem2 = mock(classOf[FrontendResource])

  private val TAG_DISPLAY_NAME = "Penguins"

  private val parentTag = Tag(display_name = "Parent")
  private val tag = Tag(parent = Some(parentTag._id), display_name = TAG_DISPLAY_NAME)

  private val loggedInUser = None

  private val noNewsitems = (Seq.empty, 0L)

  val request = new MockHttpServletRequest()

  private val modelBuilder = new TagModelBuilder(rssUrlBuilder, urlBuilder, relatedTagsService, rssfeedNewsitemService,
    contentRetrievalService, feedItemActionDecorator, commonAttributesModelBuilder, tagDAO, frontendResourceMapper)

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
    when(contentRetrievalService.getTaggedNewsitems(tag, 0, 30, loggedInUser)).thenReturn(Future.successful(noNewsitems))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(TAG_DISPLAY_NAME, mv.getModel.get("heading"))
  }

  @Test
  def mainContentShouldBeTagNewsitems {
    request.setAttribute("tags", Seq(tag))
    val tagNewsitems = Seq(newsitem1, newsitem2)
    when(contentRetrievalService.getTaggedNewsitems(tag, 0, 30, loggedInUser)).thenReturn(Future.successful((tagNewsitems, tagNewsitems.size.toLong)))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    import scala.collection.JavaConverters._
    assertEquals(tagNewsitems.asJava, mv.getModel.get(MAIN_CONTENT))
  }

  @Test
  def shouldIncludeTagParent = {
    request.setAttribute("tags", Seq(tag))
    when(contentRetrievalService.getTaggedNewsitems(tag, 0, 30, loggedInUser)).thenReturn(Future.successful(noNewsitems))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(parentTag, mv.getModel.get("parent"))
  }

  @Test
  def tagPageExtras = {
    request.setAttribute("tags", Seq(tag))
    val mv = new ModelAndView()

    val geotagged = Seq(FrontendNewsitem(id = "123", place = Some(Geocode(address = Some("Somewhere")))))
    when(contentRetrievalService.getGeotaggedNewsitemsForTag(tag, 30, loggedInUser = None)).thenReturn(Future.successful(geotagged))
    when(contentRetrievalService.getTaggedWebsites(tag, 500, loggedInUser = None)).thenReturn(Future.successful(Seq.empty))
    when(relatedTagsService.getRelatedTagsForTag(tag, 8, None)).thenReturn(Future.successful(Seq.empty))
    when(relatedTagsService.getRelatedPublishersForTag(tag, 8, None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getTagWatchlist(tag, None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getTaggedFeeds(tag, None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getLatestNewsitems(5, loggedInUser = None)).thenReturn(Future.successful(Seq.empty))

    val withExtras = Await.result(modelBuilder.populateExtraModelContent(request, mv, None), TenSeconds)

    import scala.collection.JavaConverters._
    assertEquals(geotagged.asJava, withExtras.getModel.get("geocoded"))
  }

}
