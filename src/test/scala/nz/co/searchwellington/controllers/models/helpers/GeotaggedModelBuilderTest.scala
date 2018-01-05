package nz.co.searchwellington.controllers.models.helpers

import java.util.List

import nz.co.searchwellington.controllers.{RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.filters.LocationParameterFilter
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertEquals, assertNull, assertTrue}
import org.junit.{Before, Test}
import org.mockito.{Mock, Mockito, MockitoAnnotations}
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.geo.model.{LatLong, Place}

object GeotaggedModelBuilderTest {
  private val TOTAL_GEOTAGGED_COUNT: Long = 512
  private val LOCATION_RESULTS_COUNT: Long = 33
}

class GeotaggedModelBuilderTest {
  @Mock private[helpers] val contentRetrievalService: ContentRetrievalService = null
  @Mock private[helpers] val urlBuilder: UrlBuilder = null
  @Mock private[helpers] val rssUrlBuilder: RssUrlBuilder = null
  @Mock private[helpers] val relatedTagsService: RelatedTagsService = null
  private[helpers] var commonAttributesModelBuilder: CommonAttributesModelBuilder = null
  @Mock private[helpers] val newsitemsNearPetoneStationFirstPage: List[FrontendResource] = null
  @Mock private[helpers] val newsitemsNearPetoneStationSecondPage: List[FrontendResource] = null
  private var request: MockHttpServletRequest = null
  private var validLocation: Place = null
  @Mock private[helpers] val invalidLocation: Place = null
  private var modelBuilder: GeotaggedModelBuilder = null

  @Before
  @throws[Exception]
  def setUp {
    MockitoAnnotations.initMocks(this)
    request = new MockHttpServletRequest
    validLocation = new Place("Petone Station", new LatLong(1.1, 2.2), null)
    commonAttributesModelBuilder = new CommonAttributesModelBuilder(contentRetrievalService)
    modelBuilder = new GeotaggedModelBuilder(contentRetrievalService, urlBuilder, rssUrlBuilder, relatedTagsService, commonAttributesModelBuilder)
  }

  @Test
  @throws[Exception]
  def testShouldBeValidForTagCommentPath {
    request.setPathInfo("/geotagged")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  @throws[Exception]
  def testShouldBeValidForTagCommentRssPath {
    request.setPathInfo("/geotagged/rss")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  @throws[Exception]
  def testShouldBeValidForTagCommentJSONPath {
    request.setPathInfo("/geotagged/json")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  @throws[Exception]
  def geotaggedNewsitemsPageShouldHavePaginationInformation {
    request.setPathInfo("/geotagged")
    Mockito.when(contentRetrievalService.getGeotaggedCount).thenReturn(GeotaggedModelBuilderTest.TOTAL_GEOTAGGED_COUNT)
    val modelAndView: ModelAndView = modelBuilder.populateContentModel(request).get
    assertEquals(0, modelAndView.getModel.get("page"))
    assertEquals(GeotaggedModelBuilderTest.TOTAL_GEOTAGGED_COUNT, modelAndView.getModel.get("main_content_total"))
  }

  @Test
  @throws[Exception]
  def locationSearchesShouldHaveNearbyNewsitemsAsTheMainContent {
    Mockito.when(contentRetrievalService.getNewsitemsNear(new LatLong(1.1, 2.2), 1.0, 0, 30)).thenReturn(newsitemsNearPetoneStationFirstPage)
    request.setPathInfo("/geotagged")
    request.setAttribute(LocationParameterFilter.LOCATION, validLocation)
    val modelAndView: ModelAndView = modelBuilder.populateContentModel(request).get
    assertEquals(newsitemsNearPetoneStationFirstPage, modelAndView.getModel.get("main_content"))
  }

  @Test
  @throws[Exception]
  def locationSearchRadiusShouldBeTweakableFromTheRequestParameters {
    Mockito.when(contentRetrievalService.getNewsitemsNear(new LatLong(1.1, 2.2), 3.0, 0, 30)).thenReturn(newsitemsNearPetoneStationFirstPage)
    request.setPathInfo("/geotagged")
    request.setAttribute(LocationParameterFilter.LOCATION, validLocation)
    request.setAttribute(LocationParameterFilter.RADIUS, 3.0)
    val modelAndView: ModelAndView = modelBuilder.populateContentModel(request).get
    assertEquals(newsitemsNearPetoneStationFirstPage, modelAndView.getModel.get("main_content"))
  }

  @Test
  @throws[Exception]
  def locationSearchesShouldHavePagination {
    request.setPathInfo("/geotagged")
    Mockito.when(contentRetrievalService.getNewsitemsNearCount(new LatLong(1.1, 2.2), 1.0)).thenReturn(GeotaggedModelBuilderTest.LOCATION_RESULTS_COUNT)
    request.setAttribute(LocationParameterFilter.LOCATION, validLocation)
    val modelAndView: ModelAndView = modelBuilder.populateContentModel(request).get
    assertEquals(0, modelAndView.getModel.get("page"))
    assertEquals(GeotaggedModelBuilderTest.LOCATION_RESULTS_COUNT, modelAndView.getModel.get("main_content_total"))
  }

  @Test
  @throws[Exception]
  def locationSearchesShouldHaveCorrectContentOnSecondPaginationPage {
    request.setPathInfo("/geotagged")
    Mockito.when(contentRetrievalService.getNewsitemsNearCount(new LatLong(1.1, 2.2), 1.0)).thenReturn(GeotaggedModelBuilderTest.LOCATION_RESULTS_COUNT)
    Mockito.when(contentRetrievalService.getNewsitemsNear(new LatLong(1.1, 2.2), 1.0, 30, 30)).thenReturn(newsitemsNearPetoneStationSecondPage)
    request.setAttribute(LocationParameterFilter.LOCATION, validLocation)
    request.setAttribute("page", 2)
    val modelAndView: ModelAndView = modelBuilder.populateContentModel(request).get
    assertEquals(2, modelAndView.getModel.get("page"))
    assertEquals(newsitemsNearPetoneStationSecondPage, modelAndView.getModel.get("main_content"))
  }

  @Test
  @throws[Exception]
  def locationSearchShouldNotSetLocationWasInvalid {
    request.setPathInfo("/geotagged")
    Mockito.when(invalidLocation.getLatLong).thenReturn(null)
    request.setAttribute(LocationParameterFilter.LOCATION, invalidLocation)
    val modelAndView: ModelAndView = modelBuilder.populateContentModel(request).get
    assertNull(modelAndView.getModel.get("location"))
  }
}