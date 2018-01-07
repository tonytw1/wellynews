package nz.co.searchwellington.controllers.models.helpers

import java.util
import java.util.List

import nz.co.searchwellington.controllers.models.GeotaggedNewsitemExtractor
import nz.co.searchwellington.controllers.{RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.model.Website
import nz.co.searchwellington.model.frontend.{FrontendNewsitem, FrontendResource}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import nz.co.searchwellington.views.GeocodeToPlaceMapper
import org.junit.Assert.assertEquals
import org.junit.{Before, Test}
import org.mockito.{Mock, Mockito, MockitoAnnotations}
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.geo.model.Place

class PublisherModelBuilderTest {

  @Mock private[models] var rssUrlBuilder: RssUrlBuilder = null
  @Mock private[models] var urlBuilder: UrlBuilder = null
  @Mock private[models] var relatedTagsService: RelatedTagsService = null
  @Mock private[models] var contentRetrievalService: ContentRetrievalService = null
  @Mock private[models] var geotaggedNewsitemExtractor: GeotaggedNewsitemExtractor = null
  @Mock private[models] var geocodeToPlaceMapper: GeocodeToPlaceMapper = null
  @Mock private[models] var commonAttributesModelBuilder: CommonAttributesModelBuilder = null
  @Mock private[models] var publisher: Website = null
  @Mock private[models] var newsitem: FrontendNewsitem = null
  @Mock private[models] var geotaggedNewsitem: FrontendNewsitem = null
  @Mock private[models] var geotag: Place = null

  private[models] var request: MockHttpServletRequest = null

  @Before def setup {
    MockitoAnnotations.initMocks(this)
    request = new MockHttpServletRequest
    request.setAttribute("publisher", publisher)
    Mockito.when(geotaggedNewsitem.getPlace).thenReturn(geotag)
  }

  @SuppressWarnings(Array("unchecked"))
  @Test
  @throws(classOf[Exception])
  def shouldHightlightPublishersGeotaggedContent {
    val publisherNewsitems: List[FrontendNewsitem] = new util.ArrayList();
    publisherNewsitems.add(newsitem)
    publisherNewsitems.add(geotaggedNewsitem)
    val geotaggedNewsitems: List[FrontendNewsitem] = new util.ArrayList();
    geotaggedNewsitems.add(geotaggedNewsitem)

    import scala.collection.JavaConversions._
    Mockito.when(geotaggedNewsitemExtractor.extractGeotaggedItems(publisherNewsitems)).thenReturn(geotaggedNewsitems)
    val modelBuilder = new PublisherModelBuilder(rssUrlBuilder, relatedTagsService, contentRetrievalService, urlBuilder, geotaggedNewsitemExtractor, geocodeToPlaceMapper, commonAttributesModelBuilder)
    val mv: ModelAndView = new ModelAndView
    mv.addObject("main_content", publisherNewsitems)
    modelBuilder.populateExtraModelContent(request, mv)
    val geotaggedPublisherNewsitems: List[FrontendResource] = mv.getModel.get("geocoded").asInstanceOf[List[FrontendResource]]
    assertEquals(geotaggedPublisherNewsitems, geotaggedNewsitems)
    assertEquals(geotaggedNewsitem, geotaggedPublisherNewsitems.get(0))
  }

}