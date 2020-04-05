package nz.co.searchwellington.controllers.models.helpers

import java.util.UUID

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.GeotaggedNewsitemExtractor
import nz.co.searchwellington.controllers.{RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.model.frontend.{FrontendNewsitem, FrontendResource, FrontendWebsite}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{ArchiveLink, Geocode, PublisherArchiveLink, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.joda.time.{DateTime, Interval}
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class PublisherModelBuilderTest extends ReasonableWaits {

  private val rssUrlBuilder = mock(classOf[RssUrlBuilder])
  private val urlBuilder = mock(classOf[UrlBuilder])
  private val relatedTagsService = mock(classOf[RelatedTagsService])
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val geotaggedNewsitemExtractor = mock(classOf[GeotaggedNewsitemExtractor])
  private val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])
  private val frontendResourceMapper = mock(classOf[FrontendResourceMapper])

  private val publisher = Website()
  private val frontendPublisher = FrontendWebsite(id = UUID.randomUUID().toString)

  private val modelBuilder = new PublisherModelBuilder(rssUrlBuilder, relatedTagsService, contentRetrievalService, urlBuilder, geotaggedNewsitemExtractor,
    commonAttributesModelBuilder, frontendResourceMapper)

  @Test
  def shouldHighlightPublishersGeotaggedContent {
    val loggedInUser = None

    val newsitem = FrontendNewsitem(id = UUID.randomUUID().toString)
    val geotaggedNewsitem = FrontendNewsitem(id = UUID.randomUUID().toString, place = Some(Geocode(address = Some("Somewhere"))))

    val publisherNewsitems = Seq(newsitem, geotaggedNewsitem)
    val geotaggedNewsitems = Seq(geotaggedNewsitem)

    when(contentRetrievalService.getPublisherNewsitems(publisher, 30, 0, loggedInUser)).thenReturn(Future.successful((publisherNewsitems, publisherNewsitems.size.toLong)))
    when(contentRetrievalService.getPublisherFeeds(publisher, loggedInUser)).thenReturn(Future.successful(Seq.empty))

    when(geotaggedNewsitemExtractor.extractGeotaggedItems(publisherNewsitems)).thenReturn(geotaggedNewsitems)
    when(relatedTagsService.getRelatedTagsForPublisher(publisher, None)).thenReturn(Future.successful(Seq()))
    when(frontendResourceMapper.mapFrontendWebsite(publisher)).thenReturn(Future.successful(frontendPublisher))

    val request = new MockHttpServletRequest
    request.setAttribute("publisher", publisher)

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    val geotaggedPublisherNewsitemsOnModel = mv.getModel.get("geocoded").asInstanceOf[java.util.List[FrontendResource]]
    assertEquals(geotaggedNewsitem, geotaggedPublisherNewsitemsOnModel.get(0))
  }

  @Test
  def extraContentIncludesPublisherArchiveLinks(): Unit = {
    val request = new MockHttpServletRequest
    request.setAttribute("publisher", publisher)
    val mv = new ModelAndView().addObject("publisher", frontendPublisher)

    val july = new DateTime(2020, 7, 1, 0, 0)
    val monthOfJuly = new Interval(july, july.plusMonths(1))

    val archiveLinks = Seq(ArchiveLink(count = 2, interval = monthOfJuly))
    when(contentRetrievalService.getPublisherArchiveMonths(publisher, None)).thenReturn(Future.successful(archiveLinks))
    when(contentRetrievalService.getPublisherWatchlist(publisher, None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getLatestNewsitems(maxItems = 5, loggedInUser = None)).thenReturn(Future.successful(Seq.empty))
    when(relatedTagsService.getRelatedTagsForPublisher(publisher, None)).thenReturn(Future.successful(Seq.empty))

    val withExtras = Await.result(modelBuilder.populateExtraModelContent(request, mv, null), TenSeconds)

    val publisherArchiveLinksOnExtras = withExtras.getModel.get("publisher_archive_links").asInstanceOf[java.util.List[PublisherArchiveLink]]
    assertEquals(1, publisherArchiveLinksOnExtras.size())

    val firstPublisherLink = publisherArchiveLinksOnExtras.get(0)
    assertEquals(frontendPublisher, firstPublisherLink.publisher)
    assertEquals(monthOfJuly, firstPublisherLink.interval)
    assertEquals(2, firstPublisherLink.count)
  }

}
