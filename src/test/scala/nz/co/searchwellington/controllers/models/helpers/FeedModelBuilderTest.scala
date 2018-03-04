package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.controllers.models.GeotaggedNewsitemExtractor
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, RssfeedNewsitemService}
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.model.frontend.{FeedNewsitemForAcceptance, FrontendNewsitem, FrontendResource}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.when
import org.mockito.{Mock, MockitoAnnotations}
import org.springframework.mock.web.MockHttpServletRequest
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

class FeedModelBuilderTest {
  @Mock private[models] var rssfeedNewsitemService: RssfeedNewsitemService = null
  @Mock private[models] var contentRetrievalService: ContentRetrievalService = null
  @Mock private[models] var geotaggedNewsitemExtractor: GeotaggedNewsitemExtractor = null
  @Mock private[models] var feedItemLocalCopyDecorator: FeedItemLocalCopyDecorator = null
  @Mock private[models] var frontendResourceMapper: FrontendResourceMapper = null
  private[models] var commonAttributesModelBuilder: CommonAttributesModelBuilder = null
  @Mock private[models] var feed: Feed = null
  @Mock private[models] var feedNewsitems: Seq[FeedItem] = null
  @Mock private[models] var feedNewsitemsDecoratedWithLocalCopyAndSuppressionInformation: Seq[FeedNewsitemForAcceptance] = null
  @Mock private[models] var geotaggedFeedNewsitems: Seq[FrontendNewsitem] = null
  @Mock private[models] var frontendFeed: FrontendResource = null
  private[models] var request: MockHttpServletRequest = null
  private[models] var modelBuilder: FeedModelBuilder = null

  @Before
  @throws(classOf[Exception])
  def setUp {
    MockitoAnnotations.initMocks(this)
    when(rssfeedNewsitemService.getFeedNewsitems(feed)).thenReturn(feedNewsitems)
    when(feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(feedNewsitems)).thenReturn(feedNewsitemsDecoratedWithLocalCopyAndSuppressionInformation)
    when(feedNewsitems.size).thenReturn(10)
    request = new MockHttpServletRequest
    request.setAttribute("feedAttribute", feed)
    request.setPathInfo("/feed/someonesfeed")
    commonAttributesModelBuilder = new CommonAttributesModelBuilder(contentRetrievalService)

    modelBuilder = new FeedModelBuilder(rssfeedNewsitemService, contentRetrievalService, geotaggedNewsitemExtractor, feedItemLocalCopyDecorator, frontendResourceMapper, commonAttributesModelBuilder)
  }

  @Test
  @throws(classOf[Exception])
  def feedPathsAreValid {
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  @throws(classOf[Exception])
  def shouldPopulateFrontendFeedFromRequestAttribute {
    when(frontendResourceMapper.createFrontendResourceFrom(feed)).thenReturn(frontendFeed)
    when(geotaggedNewsitemExtractor.extractGeotaggedItemsFromFeedNewsitems(feedNewsitems)).thenReturn(Seq())

    val mv = modelBuilder.populateContentModel(request).get

    assertEquals(frontendFeed, mv.getModel.get("feed"))
  }

  @Test
  @throws(classOf[Exception])
  def shouldPopulateMainContentWithFeedItemsDecoratedWithLocalCopySuppressionInformation {
    when(geotaggedNewsitemExtractor.extractGeotaggedItemsFromFeedNewsitems(feedNewsitems)).thenReturn(Seq())

    val mv = modelBuilder.populateContentModel(request).get

    assertEquals(feedNewsitemsDecoratedWithLocalCopyAndSuppressionInformation, mv.getModel.get("main_content"))
  }

  @Test
  @throws(classOf[Exception])
  def shouldPushGeotaggedFeeditemsOntoTheModelSeperately {
    when(geotaggedNewsitemExtractor.extractGeotaggedItemsFromFeedNewsitems(feedNewsitems)).thenReturn(geotaggedFeedNewsitems)
    val mv = modelBuilder.populateContentModel(request).get
    modelBuilder.populateExtraModelContent(request, mv)
    assertEquals(geotaggedFeedNewsitems, mv.getModel.get("geocoded"))
  }

}
