package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.GeotaggedNewsitemExtractor
import nz.co.searchwellington.feeds.FeedItemActionDecorator
import nz.co.searchwellington.feeds.whakaoko.model.{FeedItem, LatLong, Place, Subscription}
import nz.co.searchwellington.feeds.whakaoko.{WhakaokoFeedReader, WhakaokoService}
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.model.frontend.{FrontendFeed, FrontendFeedItem}
import nz.co.searchwellington.model.geo.Geocode
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.{BeforeEach, Test}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

class FeedModelBuilderTest extends ReasonableWaits with ContentFields {
  private val whakaokoFeedReader = mock(classOf[WhakaokoFeedReader])
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val geotaggedNewsitemExtractor = new GeotaggedNewsitemExtractor()
  private val feedItemActionDecorator = mock(classOf[FeedItemActionDecorator])
  private val frontendResourceMapper = mock(classOf[FrontendResourceMapper])
  private val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])
  private val whakaokoService = mock(classOf[WhakaokoService])

  private val whakaokoSubscription = Subscription(id = "a-whakaoko-subscription-id", name = None, channelId = "", url = "http://somewhere/rss", lastRead = None, latestItemDate = None)
  private val feed = Feed(id = UUID.randomUUID().toString, page = "http://localhost/a-feed", whakaokoSubscription = Some("a-whakaoko-subscription-id"))

  private val somePlace = Some(Place(latLong = Some(nz.co.searchwellington.feeds.whakaoko.model.LatLong(52.0, 0.0))))

  private val feedItem = FeedItem(id = UUID.randomUUID().toString, subscriptionId = "a-whakaoko-subscription-id", url = "http://localhost/a-feeditem", title = Some("A feed item"), place = somePlace)
  private val anotherFeedItem = FeedItem(id = UUID.randomUUID().toString, subscriptionId = "a-whakaoko-subscription-id", url = "http://localhost/another-feeditem", title = Some("A feed item"))
  private val feeditems = Seq(feedItem, anotherFeedItem)

  private val frontendFeedItem = FrontendFeedItem(id = UUID.randomUUID().toString, name = "A feed item", url = "http://localhost/a-feeditem", date = None,
    geocode = Some(Geocode(latLong = Some(nz.co.searchwellington.model.geo.LatLong(52.0, 0.0))))
  )
  private val anotherFrontendFeedItem = FrontendFeedItem(id = UUID.randomUUID().toString, name = "Another feed item", url = "http://localhost/a-feeditem", date = None)

  private val feedItemWithActions =  FrontendFeedItem(id = UUID.randomUUID().toString, name = "A feed item", url = "http://localhost/a-feeditem", date = None,
    geocode = Some(Geocode(latLong = Some(nz.co.searchwellington.model.geo.LatLong(52.0, 0.0))))
  )
  private val anotherFeedItemWithActions = FrontendFeedItem(id = UUID.randomUUID().toString, name = "Another feed item", url = "http://localhost/a-feeditem", date = None)

  private val frontendFeed = mock(classOf[FrontendFeed])

  private val loggedInUser = None

  private val request = new MockHttpServletRequest

  private implicit val currentSpan: Span = Span.current()

  val modelBuilder = new FeedModelBuilder(contentRetrievalService, geotaggedNewsitemExtractor,
    feedItemActionDecorator, frontendResourceMapper, commonAttributesModelBuilder,
    whakaokoFeedReader, whakaokoService)

  @BeforeEach
  def setUp(): Unit = {
    when(whakaokoFeedReader.fetchFeedItems(feed)).thenReturn(Future.successful(Right((feeditems, feeditems.size.toLong))))

    when(feedItemActionDecorator.withFeedItemSpecificActions(feed, frontendFeedItem, None)).thenReturn(Future.successful(feedItemWithActions))
    when(feedItemActionDecorator.withFeedItemSpecificActions(feed, anotherFrontendFeedItem, None)).thenReturn(Future.successful(anotherFeedItemWithActions))

    request.setAttribute("feedAttribute", feed)
    request.setRequestURI("/feed/someonesfeed")
  }

  @Test
  def feedPathsAreValid(): Unit = {
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def shouldPopulateFrontendFeedFromRequestAttribute(): Unit = {
    when(frontendResourceMapper.createFrontendResourceFrom(feed, None)).thenReturn(Future.successful(frontendFeed))
    when(frontendResourceMapper.mapFeedItem(feedItem)).thenReturn(frontendFeedItem)
    when(frontendResourceMapper.mapFeedItem(anotherFeedItem)).thenReturn(anotherFrontendFeedItem)
    when(whakaokoService.getSubscription(ArgumentMatchers.eq("a-whakaoko-subscription-id"))(any(), any())).thenReturn(Future.successful(Right(Some(whakaokoSubscription))))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(frontendFeed, mv.get("feed"))
  }

  @Test
  def shouldPopulateMainContentWithFeedItemsDecoratedWithLocalCopySuppressionInformation(): Unit = {
    when(frontendResourceMapper.createFrontendResourceFrom(feed, None)).thenReturn(Future.successful(frontendFeed))
    when(frontendResourceMapper.mapFeedItem(feedItem)).thenReturn(frontendFeedItem)
    when(frontendResourceMapper.mapFeedItem(anotherFeedItem)).thenReturn(anotherFrontendFeedItem)

    when(whakaokoFeedReader.fetchFeedItems(feed)).thenReturn(Future.successful(Right((feeditems, feeditems.size.toLong))))
    when(whakaokoService.getSubscription(ArgumentMatchers.eq("a-whakaoko-subscription-id"))(any(), any())).thenReturn(Future.successful(Right(Some(whakaokoSubscription))))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(Seq(feedItemWithActions, anotherFeedItemWithActions).asJava, mv.get(MAIN_CONTENT))
  }

  @Test
  def shouldPushGeotaggedFeeditemsOntoTheModelAsFrontendNewsitemsSeperately(): Unit = {
    when(frontendResourceMapper.createFrontendResourceFrom(feed, None)).thenReturn(Future.successful(frontendFeed))
    when(frontendResourceMapper.mapFeedItem(feedItem)).thenReturn(frontendFeedItem)
    when(frontendResourceMapper.mapFeedItem(anotherFeedItem)).thenReturn(anotherFrontendFeedItem)
    when(contentRetrievalService.getAllFeedsOrderedByLatestItemDate(loggedInUser)).thenReturn(Future.successful(Seq()))
    when(whakaokoService.getSubscription(ArgumentMatchers.eq("a-whakaoko-subscription-id"))(any(), any())).thenReturn(Future.successful(Right(Some(whakaokoSubscription))))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    modelBuilder.populateExtraModelContent(request, None)

    assertEquals(Seq(feedItemWithActions).asJava, mv.get("geocoded"))
    assertEquals(whakaokoSubscription, mv.get("subscription"), "Expected whakaoko subscription to be shown")
  }

}
