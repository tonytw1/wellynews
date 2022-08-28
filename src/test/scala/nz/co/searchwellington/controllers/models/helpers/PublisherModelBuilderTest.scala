package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.controllers.models.GeotaggedNewsitemExtractor
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.frontend.{FrontendFeed, FrontendNewsitem, FrontendResource, FrontendWebsite}
import nz.co.searchwellington.model.geo.Geocode
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.tagging.RelatedTagsService
import nz.co.searchwellington.urls.UrlBuilder
import org.joda.time.{DateTime, Interval}
import org.junit.jupiter.api.Assertions.{assertEquals, assertNotNull}
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import reactivemongo.api.bson.BSONObjectID

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

class PublisherModelBuilderTest extends ReasonableWaits with ContentFields {

  private val rssUrlBuilder = new RssUrlBuilder(new SiteInformation())
  private val urlBuilder = mock(classOf[UrlBuilder])
  private val relatedTagsService = mock(classOf[RelatedTagsService])
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val geotaggedNewsitemExtractor = new GeotaggedNewsitemExtractor
  private val commonAttributesModelBuilder = new CommonAttributesModelBuilder
  private val frontendResourceMapper = mock(classOf[FrontendResourceMapper])

  private val publisher = Website(title = "A publisher", url_words = Some("a-publisher"))
  private val frontendPublisher = FrontendWebsite(id = UUID.randomUUID().toString)

  private implicit val currentSpan: Span = Span.current()

  private val modelBuilder = new PublisherModelBuilder(rssUrlBuilder, relatedTagsService, contentRetrievalService, urlBuilder,
    geotaggedNewsitemExtractor, commonAttributesModelBuilder, frontendResourceMapper)

  @Test
  def mainContentShouldBePublisherNewsitems(): Unit = {
    val request = new MockHttpServletRequest
    request.setAttribute("publisher", publisher)
    val publisherNewsitems = Seq(FrontendNewsitem(id = "456"))
    val publisherFeeds = Seq(FrontendFeed(id = "789"))

    when(frontendResourceMapper.createFrontendResourceFrom(publisher)).thenReturn(Future.successful(frontendPublisher))
    when(contentRetrievalService.getPublisherNewsitems(publisher, 30, None)).thenReturn(Future.successful((publisherNewsitems, 1L)))
    when(contentRetrievalService.getPublisherFeeds(publisher, None)).thenReturn(Future.successful(publisherFeeds))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(publisherNewsitems.asJava, mv.get(MAIN_CONTENT))
    assertEquals(publisherFeeds.asJava, mv.get("feeds"))
    assertEquals("A publisher newsitems", mv.get("main_heading"))
    assertEquals("/a-publisher/rss", mv.get("rss_url"))
  }

  @Test
  def monthPaginationShouldBePopulatedFromDateOfFirstOverFetchedMainContentItem(): Unit = {
    val request = new MockHttpServletRequest
    request.setAttribute("publisher", publisher)
    val publisherFeeds = Seq(FrontendFeed(id = "789"))

    val maxedOutPublisherNewsitems = Range(1, 20).map { i =>
      val d = new DateTime(2022, 1, 30, 0, 0, 0)
      FrontendNewsitem(id = i.toString, date = d.minusDays(i).toDate)
    }

    when(frontendResourceMapper.createFrontendResourceFrom(publisher)).thenReturn(Future.successful(frontendPublisher))
    when(contentRetrievalService.getPublisherNewsitems(publisher, 30, None)).thenReturn(Future.successful((maxedOutPublisherNewsitems, 32L)))
    when(contentRetrievalService.getPublisherFeeds(publisher, None)).thenReturn(Future.successful(publisherFeeds))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertNotNull(mv.get("more"))
    val moreLink = mv.get("more").asInstanceOf[PublisherArchiveLink]
    assertEquals(frontendPublisher, moreLink.getPublisher)
    assertEquals(new DateTime(2022, 1, 1, 0, 0, 0).toDate, moreLink.getMonth)
  }

  @Test
  def shouldHighlightPublishersGeotaggedContent(): Unit = {
    val loggedInUser = None

    val newsitem = FrontendNewsitem(id = UUID.randomUUID().toString)
    val geotaggedNewsitem = FrontendNewsitem(id = UUID.randomUUID().toString, place = Some(Geocode(address = Some("Somewhere"))))

    val publisherNewsitems = Seq(newsitem, geotaggedNewsitem)

    when(contentRetrievalService.getPublisherNewsitems(publisher, 30, loggedInUser)).thenReturn(Future.successful((publisherNewsitems, publisherNewsitems.size.toLong)))
    when(contentRetrievalService.getPublisherFeeds(publisher, loggedInUser)).thenReturn(Future.successful(Seq.empty))

    when(relatedTagsService.getRelatedTagsForPublisher(publisher, None)).thenReturn(Future.successful(Seq()))
    when(frontendResourceMapper.createFrontendResourceFrom(publisher)).thenReturn(Future.successful(frontendPublisher))

    val request = new MockHttpServletRequest
    request.setAttribute("publisher", publisher)

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    val geotaggedPublisherNewsitemsOnModel = mv.get("geocoded").asInstanceOf[java.util.List[FrontendResource]]
    assertEquals(geotaggedNewsitem, geotaggedPublisherNewsitemsOnModel.get(0))
  }

  @Test
  def extraContentIncludesPublisherArchiveLinks(): Unit = {
    val request = new MockHttpServletRequest
    request.setAttribute("publisher", publisher)

    val july = new DateTime(2020, 7, 1, 0, 0)
    val monthOfJuly = new Interval(july, july.plusMonths(1))

    val archiveLinks = Seq(ArchiveLink(count = Some(2), interval = monthOfJuly))
    when(contentRetrievalService.getPublisherArchiveMonths(publisher, None)).thenReturn(Future.successful(archiveLinks))
    when(contentRetrievalService.getPublisherWatchlist(publisher, None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getLatestNewsitems(maxItems = 5, loggedInUser = None)).thenReturn(Future.successful(Seq.empty))
    when(relatedTagsService.getRelatedTagsForPublisher(publisher, None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getDiscoveredFeedsForPublisher(publisher)).thenReturn(Future.successful(Seq.empty))
    when(frontendResourceMapper.createFrontendResourceFrom(publisher, None)).thenReturn(Future.successful(frontendPublisher))

    val extras = Await.result(modelBuilder.populateExtraModelContent(request, None), TenSeconds)

    val publisherArchiveLinksOnExtras = extras.get("archive_links").asInstanceOf[java.util.List[PublisherArchiveLink]]
    assertEquals(1, publisherArchiveLinksOnExtras.size())
    val firstPublisherLink = publisherArchiveLinksOnExtras.get(0)
    assertEquals(frontendPublisher, firstPublisherLink.publisher)
    assertEquals(monthOfJuly, firstPublisherLink.interval)
    assertEquals(Some(2), firstPublisherLink.count)
  }

  @Test
  def extraContentShouldIncludeDiscoveredFeedsForPublisher(): Unit = {
    val request = new MockHttpServletRequest
    request.setAttribute("publisher", publisher)

    when(contentRetrievalService.getPublisherArchiveMonths(publisher, None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getPublisherWatchlist(publisher, None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getLatestNewsitems(maxItems = 5, loggedInUser = None)).thenReturn(Future.successful(Seq.empty))
    when(relatedTagsService.getRelatedTagsForPublisher(publisher, None)).thenReturn(Future.successful(Seq.empty))
    when(frontendResourceMapper.createFrontendResourceFrom(publisher, None)).thenReturn(Future.successful(frontendPublisher))
    val discoveredFeeds = Seq{
      DiscoveredFeed(url = "http://localhost/test", occurrences = Seq.empty, firstSeen = DateTime.now.toDate, publisher = Some(BSONObjectID.generate))
    }
    when(contentRetrievalService.getDiscoveredFeedsForPublisher(publisher)).thenReturn(Future.successful(discoveredFeeds))

    val extras = Await.result(modelBuilder.populateExtraModelContent(request, None), TenSeconds)

    assertEquals(discoveredFeeds.asJava, extras.get("discovered_feeds"))
  }

}
