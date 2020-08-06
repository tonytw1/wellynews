package nz.co.searchwellington.controllers.models.helpers

import java.util.UUID

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.controllers.models.GeotaggedNewsitemExtractor
import nz.co.searchwellington.feeds.whakaoko.WhakaokoService
import nz.co.searchwellington.feeds.whakaoko.model.{FeedItem, Subscription}
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, FeeditemToNewsitemService, RssfeedNewsitemService}
import nz.co.searchwellington.model.frontend.{FrontendFeed, FrontendNewsitem}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Feed, Geocode, Newsitem}
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Matchers
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class FeedModelBuilderTest extends ReasonableWaits with ContentFields {
  private val rssfeedNewsitemService = mock(classOf[RssfeedNewsitemService])
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val geotaggedNewsitemExtractor = new GeotaggedNewsitemExtractor()
  private val feedItemLocalCopyDecorator = mock(classOf[FeedItemLocalCopyDecorator])
  private val frontendResourceMapper = mock(classOf[FrontendResourceMapper])
  private val feeditemToNewsitemService = mock(classOf[FeeditemToNewsitemService])
  private val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])
  private val whakaokoService = mock(classOf[WhakaokoService])

  var feed = Feed(id = UUID.randomUUID().toString, page = "http://localhost/a-feed")

  private val feedItem = mock(classOf[FeedItem])
  private val anotherFeedItem = mock(classOf[FeedItem])
  private val feeditems = Seq(feedItem, anotherFeedItem)

  private val somePlace = Some(Geocode())

  private val newsItem = Newsitem()
  private val anotherNewsitem = Newsitem()

  private val frontendNewsitem = FrontendNewsitem(id = UUID.randomUUID().toString)
  private val anotherFrontendNewsitem = FrontendNewsitem(id = UUID.randomUUID().toString, place = somePlace)

  private val frontendNewsitemWithActions = FrontendNewsitem(id = UUID.randomUUID().toString) // TODO better example with actions
  private val anotherFrontendNewsitemWithActions = FrontendNewsitem(id = UUID.randomUUID().toString, place = somePlace)

  private val subscription = mock(classOf[Subscription])

  private val frontendFeed = mock(classOf[FrontendFeed])

  private val loggedInUser = None

  var request: MockHttpServletRequest = null

  val modelBuilder = new FeedModelBuilder(rssfeedNewsitemService, contentRetrievalService, geotaggedNewsitemExtractor,
    feedItemLocalCopyDecorator, frontendResourceMapper, commonAttributesModelBuilder, feeditemToNewsitemService, whakaokoService)

  @Before
  def setUp {
    when(rssfeedNewsitemService.getFeedItemsAndDetailsFor(feed)).thenReturn(Future.successful(Right((feeditems, subscription))))

    when(feeditemToNewsitemService.makeNewsitemFromFeedItem(feedItem, feed)).thenReturn(newsItem)
    when(feeditemToNewsitemService.makeNewsitemFromFeedItem(anotherFeedItem, feed)).thenReturn(anotherNewsitem)

    when(frontendResourceMapper.mapFrontendResource(newsItem, newsItem.geocode)).thenReturn(Future.successful(frontendNewsitem))
    when(frontendResourceMapper.mapFrontendResource(anotherNewsitem, newsItem.geocode)).thenReturn(Future.successful(anotherFrontendNewsitem))

    when(feedItemLocalCopyDecorator.withFeedItemSpecificActions(Seq(frontendNewsitem, anotherFrontendNewsitem), None)).
      thenReturn(Future.successful(Seq(frontendNewsitemWithActions, anotherFrontendNewsitemWithActions)))

    request = new MockHttpServletRequest
    request.setAttribute("feedAttribute", feed)
    request.setRequestURI("/feed/someonesfeed")
  }

  @Test
  def feedPathsAreValid {
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def shouldPopulateFrontendFeedFromRequestAttribute {
    when(frontendResourceMapper.createFrontendResourceFrom(feed, None)).thenReturn(Future.successful(frontendFeed))
    when(whakaokoService.getWhakaokoSubscriptionByUrl(Matchers.any())(Matchers.any())).thenReturn(Future.successful(None))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(frontendFeed, mv.getModel.get("feed"))
  }

  @Test
  def shouldPopulateMainContentWithFeedItemsDecoratedWithLocalCopySuppressionInformation {
    when(frontendResourceMapper.createFrontendResourceFrom(feed, None)).thenReturn(Future.successful(frontendFeed))
    when(rssfeedNewsitemService.getFeedItemsAndDetailsFor(feed)).thenReturn(Future.successful(Right((feeditems, subscription))))
    when(whakaokoService.getWhakaokoSubscriptionByUrl(Matchers.any())(Matchers.any())).thenReturn(Future.successful(None))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    import scala.collection.JavaConverters._
    assertEquals(Seq(frontendNewsitemWithActions, anotherFrontendNewsitemWithActions).asJava, mv.getModel.get(MAIN_CONTENT))
  }

  @Test
  def shouldPushGeotaggedFeeditemsOntoTheModelAsFrontendNewsitemsSeperately {
    val whakaokoSubscription = Subscription(id = "a-subscription", name = None, channelId = "", url = "http://somewhere/rss", lastRead = None, latestItemDate = None)

    when(frontendResourceMapper.createFrontendResourceFrom(feed, None)).thenReturn(Future.successful(frontendFeed))
    when(contentRetrievalService.getAllFeedsOrderedByLatestItemDate(loggedInUser)).thenReturn(Future.successful(Seq()))
    when(whakaokoService.getWhakaokoSubscriptionByUrl(Matchers.any())(Matchers.any())).thenReturn(Future.successful(Some(whakaokoSubscription)))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    modelBuilder.populateExtraModelContent(request, mv, None)

    import scala.collection.JavaConverters._
    assertEquals(Seq(anotherFrontendNewsitemWithActions).asJava, mv.getModel.get("geocoded"))
    assertEquals("Expected whakaoko subscription to be shown", whakaokoSubscription, mv.getModel.get("subscription"))
  }

}
