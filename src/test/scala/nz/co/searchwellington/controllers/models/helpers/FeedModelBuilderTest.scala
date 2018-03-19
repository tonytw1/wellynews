package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.controllers.models.GeotaggedNewsitemExtractor
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, FeeditemToNewsitemService, RssfeedNewsitemService}
import nz.co.searchwellington.model.frontend.{FeedNewsitemForAcceptance, FrontendResource}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Feed, Newsitem}
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mock
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

class FeedModelBuilderTest {
  val rssfeedNewsitemService = mock(classOf[RssfeedNewsitemService])
  val contentRetrievalService = mock(classOf[ContentRetrievalService])
  val geotaggedNewsitemExtractor = mock(classOf[GeotaggedNewsitemExtractor])
  val feedItemLocalCopyDecorator = mock(classOf[FeedItemLocalCopyDecorator])
  val frontendResourceMapper = mock(classOf[FrontendResourceMapper])
  val feeditemToNewsitemService =  mock(classOf[FeeditemToNewsitemService])
  val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])

  var feed = Feed(page = Some("http://localhost/a-feed"))
  var feeditems: Seq[(FeedItem, Option[Feed])] = Seq()
  var feedNewsitems: Seq[Newsitem] = Seq()

  val decoratedFeedItem = mock(classOf[FeedNewsitemForAcceptance])
  val anotherDecoratedFeedItem = mock(classOf[FeedNewsitemForAcceptance])
  val feedNewsitemsDecoratedWithLocalCopyAndSuppressionInformation = Seq(decoratedFeedItem, anotherDecoratedFeedItem)

  val geotaggedFeeditem = mock(classOf[FeedItem])
  val anotherGeotaggedFeeditem = mock(classOf[FeedItem])
  val geotaggedFeedNewsitems = Seq(geotaggedFeeditem, anotherGeotaggedFeeditem)

  @Mock var frontendFeed: FrontendResource = null

  var request: MockHttpServletRequest = null

  val modelBuilder = new FeedModelBuilder(rssfeedNewsitemService, contentRetrievalService, geotaggedNewsitemExtractor,
      feedItemLocalCopyDecorator, frontendResourceMapper, commonAttributesModelBuilder, feeditemToNewsitemService)

  @Before
  @throws(classOf[Exception])
  def setUp {
    when(rssfeedNewsitemService.getFeedItemsFor(feed)).thenReturn(feeditems)
    when(feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(feedNewsitems)).thenReturn(feedNewsitemsDecoratedWithLocalCopyAndSuppressionInformation)
    request = new MockHttpServletRequest
    request.setAttribute("feedAttribute", feed)
    request.setPathInfo("/feed/someonesfeed")
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
    when(contentRetrievalService.getAllFeedsOrderByLatestItemDate).thenReturn(Seq())

    val mv = modelBuilder.populateContentModel(request).get
    modelBuilder.populateExtraModelContent(request, mv)

    assertEquals(geotaggedFeedNewsitems, mv.getModel.get("geocoded"))
  }

}
