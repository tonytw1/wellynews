package nz.co.searchwellington.controllers.models.helpers

import java.util.UUID

import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.controllers.models.GeotaggedNewsitemExtractor
import nz.co.searchwellington.feeds.reading.whakaoko.model.{FeedItem, Subscription}
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, FeeditemToNewsitemService, RssfeedNewsitemService}
import nz.co.searchwellington.model.frontend.{FeedNewsitemForAcceptance, FrontendFeed, FrontendNewsitem}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Feed, Newsitem}
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FeedModelBuilderTest {
  private val rssfeedNewsitemService = mock(classOf[RssfeedNewsitemService])
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val geotaggedNewsitemExtractor = mock(classOf[GeotaggedNewsitemExtractor])
  private val feedItemLocalCopyDecorator = mock(classOf[FeedItemLocalCopyDecorator])
  private val frontendResourceMapper = mock(classOf[FrontendResourceMapper])
  private val feeditemToNewsitemService = mock(classOf[FeeditemToNewsitemService])
  private val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])

  var feed = Feed(id = UUID.randomUUID().toString, page = Some("http://localhost/a-feed"))

  private val feedItem = mock(classOf[FeedItem])
  private val anotherFeedItem = mock(classOf[FeedItem])
  private var feeditems = Seq(feedItem, anotherFeedItem)

  private val newsItem = Newsitem()
  private val anotherNewsitem = Newsitem()

  private val feedNewsitems = Seq(newsItem, anotherNewsitem)

  private val frontendNewsitem = FrontendNewsitem(id = UUID.randomUUID().toString)
  private val anotherFrontendNewsitem = FrontendNewsitem(id = UUID.randomUUID().toString)

  private val decoratedFeedItem = FeedNewsitemForAcceptance(newsitem = frontendNewsitem, localCopy = None, suppressed = false)
  private val anotherDecoratedFeedItem = FeedNewsitemForAcceptance(newsitem = anotherFrontendNewsitem, localCopy = None, suppressed = false)
  private val feedNewsitemsDecoratedWithLocalCopyAndSuppressionInformation = Seq(decoratedFeedItem, anotherDecoratedFeedItem)

  private val subscription = mock(classOf[Subscription])

  private val frontendFeed = mock(classOf[FrontendFeed])

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
    when(geotaggedNewsitemExtractor.extractGeotaggedItems(Seq(frontendNewsitem, anotherFrontendNewsitem))).thenReturn(Seq.empty)

    val mv = modelBuilder.populateContentModel(request).get

    assertEquals(frontendFeed, mv.getModel.get("feed"))
  }

  @Test
  def shouldPopulateMainContentWithFeedItemsDecoratedWithLocalCopySuppressionInformation {
    when(rssfeedNewsitemService.getFeedItemsAndDetailsFor(feed)).thenReturn(Future.successful(Right((feeditems, subscription))))
    when(geotaggedNewsitemExtractor.extractGeotaggedItems(Seq(frontendNewsitem, anotherFrontendNewsitem))).thenReturn(Seq.empty)

    val mv = modelBuilder.populateContentModel(request).get

    import scala.collection.JavaConverters._
    assertEquals(feedNewsitemsDecoratedWithLocalCopyAndSuppressionInformation.asJava, mv.getModel.get("main_content"))
  }

  @Test
  def shouldPushGeotaggedFeeditemsOntoTheModeAsFrontendNewsitemsSeperately {
    when(geotaggedNewsitemExtractor.extractGeotaggedItems(Seq(frontendNewsitem, anotherFrontendNewsitem))).thenReturn(Seq(anotherFrontendNewsitem))
    when(contentRetrievalService.getAllFeedsOrderedByLatestItemDate(loggedInUser)).thenReturn(Future.successful(Seq()))
    val mv = modelBuilder.populateContentModel(request).get

    modelBuilder.populateExtraModelContent(request, mv)

    import scala.collection.JavaConverters._
    assertEquals(Seq(anotherFrontendNewsitem).asJava, mv.getModel.get("geocoded"))
  }

}
