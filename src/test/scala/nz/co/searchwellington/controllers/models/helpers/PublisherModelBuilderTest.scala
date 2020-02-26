package nz.co.searchwellington.controllers.models.helpers

import java.util.UUID

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.GeotaggedNewsitemExtractor
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.model.frontend.{FrontendNewsitem, FrontendResource, FrontendWebsite}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Geocode, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.{Await, Future}

class PublisherModelBuilderTest extends ReasonableWaits {

  private val rssUrlBuilder = mock(classOf[RssUrlBuilder])
  private val urlBuilder = mock(classOf[UrlBuilder])
  private val relatedTagsService = mock(classOf[RelatedTagsService])
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val geotaggedNewsitemExtractor = mock(classOf[GeotaggedNewsitemExtractor])
  private val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])
  private val frontendResourceMapper = mock(classOf[FrontendResourceMapper])
  private val loggedInUserFilter = mock(classOf[LoggedInUserFilter])

  private val publisher = Website()
  private val frontendPublisher = FrontendWebsite(id = UUID.randomUUID().toString)

  private val modelBuilder = new PublisherModelBuilder(rssUrlBuilder, relatedTagsService, contentRetrievalService, urlBuilder, geotaggedNewsitemExtractor,
    commonAttributesModelBuilder, frontendResourceMapper, loggedInUserFilter)

  @Test
  def shouldHightlightPublishersGeotaggedContent {
    val loggedInUser = None

     val newsitem = FrontendNewsitem(id = UUID.randomUUID().toString)
     val geotaggedNewsitem = FrontendNewsitem(id = UUID.randomUUID().toString, place = Some(Geocode(address = Some("Somewhere"))))

    val publisherNewsitems = Seq(newsitem, geotaggedNewsitem)
    val geotaggedNewsitems = Seq(geotaggedNewsitem)

    when(contentRetrievalService.getPublisherNewsitems(publisher, 30, 0, loggedInUser)).thenReturn(Future.successful((publisherNewsitems, publisherNewsitems.size.toLong)))
    when(contentRetrievalService.getPublisherFeeds(publisher, loggedInUser)).thenReturn(Future.successful(Seq.empty))

    when(geotaggedNewsitemExtractor.extractGeotaggedItems(publisherNewsitems)).thenReturn(geotaggedNewsitems)
    when(relatedTagsService.getRelatedLinksForPublisher(publisher)).thenReturn(Seq())
    when(frontendResourceMapper.mapFrontendWebsite(publisher)).thenReturn(frontendPublisher)

    val request = new MockHttpServletRequest
    request.setAttribute("publisher", publisher)

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    val geotaggedPublisherNewsitemsOnModel = mv.getModel.get("geocoded").asInstanceOf[java.util.List[FrontendResource]]
    assertEquals(geotaggedNewsitem, geotaggedPublisherNewsitemsOnModel.get(0))
  }

}
