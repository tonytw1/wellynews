package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.controllers.models.GeotaggedNewsitemExtractor
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, FeeditemToNewsitemService, RssfeedNewsitemService}
import nz.co.searchwellington.model.frontend.{FeedNewsitemForAcceptance, FrontendResource}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Feed, Newsitem}
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.when
import org.mockito.{Mock, MockitoAnnotations}
import org.springframework.mock.web.MockHttpServletRequest
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

class FeedModelBuilderTest {
  @Mock var rssfeedNewsitemService: RssfeedNewsitemService = null
  @Mock var contentRetrievalService: ContentRetrievalService = null
  @Mock var geotaggedNewsitemExtractor: GeotaggedNewsitemExtractor = null
  @Mock var feedItemLocalCopyDecorator: FeedItemLocalCopyDecorator = null
  @Mock var frontendResourceMapper: FrontendResourceMapper = null
  @Mock var feeditemToNewsitemService: FeeditemToNewsitemService = null
  var commonAttributesModelBuilder: CommonAttributesModelBuilder = null
  @Mock var feed: Feed = null
  @Mock var feeditems: Seq[(FeedItem, Option[Feed])] = null
  @Mock var feedNewsitems: Seq[Newsitem] = null
  @Mock var feedNewsitemsDecoratedWithLocalCopyAndSuppressionInformation: Seq[FeedNewsitemForAcceptance] = null
  @Mock var geotaggedFeedNewsitems: Seq[FeedItem] = null
  @Mock var frontendFeed: FrontendResource = null
  var request: MockHttpServletRequest = null
  var modelBuilder: FeedModelBuilder = null

  @Before
  @throws(classOf[Exception])
  def setUp {
    MockitoAnnotations.initMocks(this)
    when(rssfeedNewsitemService.getFeedItemsFor(feed)).thenReturn(feeditems)
    when(feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(feedNewsitems)).thenReturn(feedNewsitemsDecoratedWithLocalCopyAndSuppressionInformation)
    when(feedNewsitems.size).thenReturn(10)
    request = new MockHttpServletRequest
    request.setAttribute("feedAttribute", feed)
    request.setPathInfo("/feed/someonesfeed")
    commonAttributesModelBuilder = new CommonAttributesModelBuilder(contentRetrievalService)

    modelBuilder = new FeedModelBuilder(rssfeedNewsitemService, contentRetrievalService, geotaggedNewsitemExtractor,
      feedItemLocalCopyDecorator, frontendResourceMapper, commonAttributesModelBuilder, feeditemToNewsitemService)
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
    when(geotaggedNewsitemExtractor.extractGeotaggedItemsFromFeedNewsitems(feeditems.map(_._1))).thenReturn(Seq())

    val mv = modelBuilder.populateContentModel(request).get

    assertEquals(frontendFeed, mv.getModel.get("feed"))
  }

  @Test
  @throws(classOf[Exception])
  def shouldPopulateMainContentWithFeedItemsDecoratedWithLocalCopySuppressionInformation {
    when(geotaggedNewsitemExtractor.extractGeotaggedItemsFromFeedNewsitems(feeditems.map(_._1))).thenReturn(Seq())

    val mv = modelBuilder.populateContentModel(request).get

    assertEquals(feedNewsitemsDecoratedWithLocalCopyAndSuppressionInformation, mv.getModel.get("main_content"))
  }

  @Test
  @throws(classOf[Exception])
  def shouldPushGeotaggedFeeditemsOntoTheModelSeperately {
    when(geotaggedNewsitemExtractor.extractGeotaggedItemsFromFeedNewsitems(feeditems.map(_._1))).thenReturn(geotaggedFeedNewsitems)
    val mv = modelBuilder.populateContentModel(request).get
    modelBuilder.populateExtraModelContent(request, mv)
    assertEquals(geotaggedFeedNewsitems, mv.getModel.get("geocoded"))
  }

}
