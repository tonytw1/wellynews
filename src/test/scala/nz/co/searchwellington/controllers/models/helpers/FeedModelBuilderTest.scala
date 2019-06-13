package nz.co.searchwellington.controllers.models.helpers

import java.util.UUID

import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.controllers.models.GeotaggedNewsitemExtractor
import nz.co.searchwellington.feeds.reading.whakaoko.model.{FeedItem, Subscription}
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, FeeditemToNewsitemService, RssfeedNewsitemService}
import nz.co.searchwellington.model.frontend.{FeedNewsitemForAcceptance, FrontendFeed}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Feed, Newsitem}
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class FeedModelBuilderTest {
  val rssfeedNewsitemService = mock(classOf[RssfeedNewsitemService])
  val contentRetrievalService = mock(classOf[ContentRetrievalService])
  val geotaggedNewsitemExtractor = mock(classOf[GeotaggedNewsitemExtractor])
  val feedItemLocalCopyDecorator = mock(classOf[FeedItemLocalCopyDecorator])
  val frontendResourceMapper = mock(classOf[FrontendResourceMapper])
  val feeditemToNewsitemService =  mock(classOf[FeeditemToNewsitemService])
  val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])

  var feed = Feed(id = UUID.randomUUID().toString, page = Some("http://localhost/a-feed"))

  val feedItem = mock(classOf[FeedItem])
  val anotherFeedItem = mock(classOf[FeedItem])
  var feeditems = Seq(feedItem, anotherFeedItem)

  val newsItem = mock(classOf[Newsitem])
  val anotherNewsitem = mock(classOf[Newsitem])
  var feedNewsitems = Seq(newsItem, anotherNewsitem)

  val decoratedFeedItem = mock(classOf[FeedNewsitemForAcceptance])
  val anotherDecoratedFeedItem = mock(classOf[FeedNewsitemForAcceptance])
  val feedNewsitemsDecoratedWithLocalCopyAndSuppressionInformation: Seq[FeedNewsitemForAcceptance] = Seq(decoratedFeedItem, anotherDecoratedFeedItem)

  val geotaggedFeeditem = mock(classOf[FeedItem])
  val anotherGeotaggedFeeditem = mock(classOf[FeedItem])
  val geotaggedFeedNewsitems = Seq(geotaggedFeeditem, anotherGeotaggedFeeditem)

  val subscription = mock(classOf[Subscription])

  val frontendFeed = mock(classOf[FrontendFeed])

  private val loggedInUserFilter = mock(classOf[LoggedInUserFilter])
  private val loggedInUser = None

  var request: MockHttpServletRequest = null

  val modelBuilder = new FeedModelBuilder(rssfeedNewsitemService, contentRetrievalService, geotaggedNewsitemExtractor,
      feedItemLocalCopyDecorator, frontendResourceMapper, commonAttributesModelBuilder, feeditemToNewsitemService, loggedInUserFilter)

  @Before
  def setUp {
    when(rssfeedNewsitemService.getFeedItemsAndDetailsFor(feed)).thenReturn(Future.successful(Right((feeditems, subscription))))

    when(feeditemToNewsitemService.makeNewsitemFromFeedItem(feedItem, feed)).thenReturn(newsItem)
    when(feeditemToNewsitemService.makeNewsitemFromFeedItem(anotherFeedItem, feed)).thenReturn(anotherNewsitem)

    when(feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(feedNewsitems)).thenReturn(Future.successful(feedNewsitemsDecoratedWithLocalCopyAndSuppressionInformation))
    request = new MockHttpServletRequest
    request.setAttribute("feedAttribute", feed)
    request.setPathInfo("/feed/someonesfeed")
  }

  @Test
  def feedPathsAreValid {
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def shouldPopulateFrontendFeedFromRequestAttribute {
    when(frontendResourceMapper.createFrontendResourceFrom(feed)).thenReturn(frontendFeed)
    when(geotaggedNewsitemExtractor.extractGeotaggedItemsFromFeedNewsitems(feeditems)).thenReturn(Seq())

    val mv = modelBuilder.populateContentModel(request).get

    assertEquals(frontendFeed, mv.getModel.get("feed"))
  }

  @Test
  def shouldPopulateMainContentWithFeedItemsDecoratedWithLocalCopySuppressionInformation {
    when(rssfeedNewsitemService.getFeedItemsAndDetailsFor(feed)).thenReturn(Future.successful(Right((feeditems, subscription))))
    when(feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(feedNewsitems)).thenReturn(Future.successful(feedNewsitemsDecoratedWithLocalCopyAndSuppressionInformation))
    when(geotaggedNewsitemExtractor.extractGeotaggedItemsFromFeedNewsitems(feeditems)).thenReturn(Seq())

    val mv = modelBuilder.populateContentModel(request).get

    import scala.collection.JavaConverters._
    assertEquals(feedNewsitemsDecoratedWithLocalCopyAndSuppressionInformation.asJava, mv.getModel.get("main_content"))
  }

  @Test
  def shouldPushGeotaggedFeeditemsOntoTheModelSeperately {
    when(geotaggedNewsitemExtractor.extractGeotaggedItemsFromFeedNewsitems(feeditems)).thenReturn(geotaggedFeedNewsitems)
    when(contentRetrievalService.getAllFeedsOrderedByLatestItemDate(loggedInUser)).thenReturn(Future.successful(Seq()))
    val mv = modelBuilder.populateContentModel(request).get

    modelBuilder.populateExtraModelContent(request, mv)

    import scala.collection.JavaConverters._
    assertEquals(geotaggedFeedNewsitems.asJava, mv.getModel.get("geocoded"))
  }

}
