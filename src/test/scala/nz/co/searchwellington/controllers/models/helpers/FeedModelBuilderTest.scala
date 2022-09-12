package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.GeotaggedNewsitemExtractor
import nz.co.searchwellington.feeds.whakaoko.model.{FeedItem, Subscription}
import nz.co.searchwellington.feeds.whakaoko.{WhakaokoFeedReader, WhakaokoService}
import nz.co.searchwellington.feeds.{FeedItemActionDecorator, FeeditemToNewsitemService}
import nz.co.searchwellington.model.frontend.{FrontendFeed, FrontendNewsitem}
import nz.co.searchwellington.model.geo.Geocode
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Feed, Newsitem}
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
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
  private val feeditemToNewsitemService = mock(classOf[FeeditemToNewsitemService])
  private val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])
  private val whakaokoService = mock(classOf[WhakaokoService])

  private val whakaokoSubscription = Subscription(id = "a-whakaoko-subscription-id", name = None, channelId = "", url = "http://somewhere/rss", lastRead = None, latestItemDate = None)
  private val feed = Feed(id = UUID.randomUUID().toString, page = "http://localhost/a-feed", whakaokoSubscription = Some("a-whakaoko-subscription-id"))

  private val feedItem = mock(classOf[FeedItem])
  private val anotherFeedItem = mock(classOf[FeedItem])
  private val feeditems = Seq(feedItem, anotherFeedItem)

  private val somePlace = Some(Geocode())

  private val newsItem = Newsitem()
  private val anotherNewsitem = Newsitem()

  private val frontendNewsitem = FrontendNewsitem(id = UUID.randomUUID().toString)
  private val anotherFrontendNewsitem = FrontendNewsitem(id = UUID.randomUUID().toString, geocode = somePlace)

  private val frontendNewsitemWithActions = FrontendNewsitem(id = UUID.randomUUID().toString, tags = None, handTags = None) // TODO better example with actions
  private val anotherFrontendNewsitemWithActions = FrontendNewsitem(id = UUID.randomUUID().toString, geocode = somePlace, tags = None, handTags = None)

  private val frontendFeed = mock(classOf[FrontendFeed])

  private val loggedInUser = None

  private val request = new MockHttpServletRequest

  private implicit val currentSpan: Span = Span.current()

  val modelBuilder = new FeedModelBuilder(contentRetrievalService, geotaggedNewsitemExtractor,
    feedItemActionDecorator, frontendResourceMapper, commonAttributesModelBuilder, feeditemToNewsitemService,
    whakaokoFeedReader, whakaokoService)

  @BeforeEach
  def setUp(): Unit = {
    when(whakaokoFeedReader.fetchFeedItems(feed)).thenReturn(Future.successful(Right((feeditems, feeditems.size.toLong))))

    when(feeditemToNewsitemService.makeNewsitemFromFeedItem(feedItem, feed)).thenReturn(Some(newsItem))
    when(feeditemToNewsitemService.makeNewsitemFromFeedItem(anotherFeedItem, feed)).thenReturn(Some(anotherNewsitem))

    when(frontendResourceMapper.mapFrontendResource(newsItem, newsItem.geocode, Seq.empty, Seq.empty, loggedInUser)).thenReturn(Future.successful(frontendNewsitem))
    when(frontendResourceMapper.mapFrontendResource(anotherNewsitem, newsItem.geocode, Seq.empty, Seq.empty, loggedInUser)).thenReturn(Future.successful(anotherFrontendNewsitem))

    when(feedItemActionDecorator.withFeedItemSpecificActions(Seq(frontendNewsitem, anotherFrontendNewsitem), None)).
      thenReturn(Future.successful(Seq(frontendNewsitemWithActions, anotherFrontendNewsitemWithActions)))

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
    when(whakaokoService.getSubscription(ArgumentMatchers.eq("a-whakaoko-subscription-id"))(any(), any())).thenReturn(Future.successful(Right(Some(whakaokoSubscription))))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(frontendFeed, mv.get("feed"))
  }

  @Test
  def shouldPopulateMainContentWithFeedItemsDecoratedWithLocalCopySuppressionInformation(): Unit = {
    when(frontendResourceMapper.createFrontendResourceFrom(feed, None)).thenReturn(Future.successful(frontendFeed))
    when(whakaokoFeedReader.fetchFeedItems(feed)).thenReturn(Future.successful(Right((feeditems, feeditems.size.toLong))))
    when(whakaokoService.getSubscription(ArgumentMatchers.eq("a-whakaoko-subscription-id"))(any(), any())).thenReturn(Future.successful(Right(Some(whakaokoSubscription))))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(Seq(frontendNewsitemWithActions, anotherFrontendNewsitemWithActions).asJava, mv.get(MAIN_CONTENT))
  }

  @Test
  def shouldPushGeotaggedFeeditemsOntoTheModelAsFrontendNewsitemsSeperately(): Unit = {
    when(frontendResourceMapper.createFrontendResourceFrom(feed, None)).thenReturn(Future.successful(frontendFeed))
    when(contentRetrievalService.getAllFeedsOrderedByLatestItemDate(loggedInUser)).thenReturn(Future.successful(Seq()))
    when(whakaokoService.getSubscription(ArgumentMatchers.eq("a-whakaoko-subscription-id"))(any(), any())).thenReturn(Future.successful(Right(Some(whakaokoSubscription))))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    modelBuilder.populateExtraModelContent(request, None)

    assertEquals(Seq(anotherFrontendNewsitemWithActions).asJava, mv.get("geocoded"))
    assertEquals(whakaokoSubscription, mv.get("subscription"), "Expected whakaoko subscription to be shown")
  }

}
