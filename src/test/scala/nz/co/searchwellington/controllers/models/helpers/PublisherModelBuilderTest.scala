package nz.co.searchwellington.controllers.models.helpers

import java.util.UUID

import nz.co.searchwellington.controllers.models.GeotaggedNewsitemExtractor
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.model.frontend.{FrontendNewsitem, FrontendResource, FrontendWebsite}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Geocode, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.assertEquals
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.Future

class PublisherModelBuilderTest {

  private val rssUrlBuilder = mock(classOf[RssUrlBuilder])
  private val urlBuilder = mock(classOf[UrlBuilder])
  private val relatedTagsService: RelatedTagsService = mock(classOf[RelatedTagsService])
  private val contentRetrievalService: ContentRetrievalService = mock(classOf[ContentRetrievalService])
  private val geotaggedNewsitemExtractor: GeotaggedNewsitemExtractor = mock(classOf[GeotaggedNewsitemExtractor])
  private val commonAttributesModelBuilder: CommonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])
  private val frontendResourceMapper: FrontendResourceMapper = mock(classOf[FrontendResourceMapper])
  private val loggedInUserFilter: LoggedInUserFilter = mock(classOf[LoggedInUserFilter])


  private val publisher: Website = Website()
  private val frontendPublisher = FrontendWebsite(id = UUID.randomUUID().toString)
  private val newsitem: FrontendNewsitem = FrontendNewsitem()
  private val geotag = Geocode(address = Some("Somewhere"))
  private val geotaggedNewsitem: FrontendNewsitem = FrontendNewsitem(place = Some(geotag))

  private var request: MockHttpServletRequest = null

  private val modelBuilder = new PublisherModelBuilder(rssUrlBuilder, relatedTagsService, contentRetrievalService, urlBuilder, geotaggedNewsitemExtractor,
    commonAttributesModelBuilder, frontendResourceMapper, loggedInUserFilter)


  @Before def setup {
    request = new MockHttpServletRequest
    request.setAttribute("publisher", publisher)
    //    when(geotaggedNewsitem.place).thenReturn(Some(geotag))
  }

  @SuppressWarnings(Array("unchecked"))
  @Test
  @throws(classOf[Exception])
  def shouldHightlightPublishersGeotaggedContent {
    val publisherNewsitems: Seq[FrontendNewsitem] = Seq(newsitem, geotaggedNewsitem)
    val geotaggedNewsitems: Seq[FrontendResource] = Seq(geotaggedNewsitem)

    val loggedInUser = None

    when(contentRetrievalService.getPublisherNewsitems(publisher, 30, 0, loggedInUser)).thenReturn(Future.successful((publisherNewsitems, publisherNewsitems.size.toLong)))
    when(contentRetrievalService.getPublisherFeeds(publisher, loggedInUser)).thenReturn(Future.successful(Seq.empty))

    when(geotaggedNewsitemExtractor.extractGeotaggedItems(publisherNewsitems)).thenReturn(geotaggedNewsitems)
    when(relatedTagsService.getRelatedLinksForPublisher(publisher)).thenReturn(Seq())
    when(frontendResourceMapper.mapFrontendWebsite(publisher)).thenReturn(frontendPublisher)

    val mv = modelBuilder.populateContentModel(request).get

    val value = mv.getModel.get("geocoded")
    val geotaggedPublisherNewsitems: java.util.List[FrontendResource] = value.asInstanceOf[java.util.List[FrontendResource]]
    assertEquals(geotaggedNewsitem, geotaggedPublisherNewsitems.get(0))
  }

}
