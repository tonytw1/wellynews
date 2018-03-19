package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.controllers.{RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.filters.LocationParameterFilter
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertEquals, assertNull, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.when
import org.mockito.{Mock, MockitoAnnotations}
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.geo.model.{LatLong, Place}
import org.mockito.Matchers._

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
  @Mock private[helpers] val newsitemsNearPetoneStationFirstPage: Seq[FrontendResource] = null
  @Mock private[helpers] val newsitemsNearPetoneStationSecondPage: Seq[FrontendResource] = null
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
    when(contentRetrievalService.getGeotaggedCount).thenReturn(GeotaggedModelBuilderTest.TOTAL_GEOTAGGED_COUNT)

    val modelAndView = modelBuilder.populateContentModel(request).get

    assertEquals(1, modelAndView.getModel.get("page"))
    assertEquals(GeotaggedModelBuilderTest.TOTAL_GEOTAGGED_COUNT, modelAndView.getModel.get("main_content_total"))
  }

  @Test
  @throws[Exception]
  def locationSearchesShouldHaveNearbyNewsitemsAsTheMainContent {
    when(contentRetrievalService.getNewsitemsNear(new LatLong(1.1, 2.2), 1.0, 0, 30)).thenReturn(newsitemsNearPetoneStationFirstPage)
    when(relatedTagsService.getRelatedTagsForLocation(any(), any(), any())).thenReturn(Seq())
    when(relatedTagsService.getRelatedPublishersForLocation(any(), any())).thenReturn(Seq())
    request.setPathInfo("/geotagged")
    request.setAttribute(LocationParameterFilter.LOCATION, validLocation)

    val modelAndView = modelBuilder.populateContentModel(request).get

    assertEquals(newsitemsNearPetoneStationFirstPage, modelAndView.getModel.get("main_content"))
  }

  @Test
  @throws[Exception]
  def locationSearchRadiusShouldBeTweakableFromTheRequestParameters {
    when(contentRetrievalService.getNewsitemsNear(new LatLong(1.1, 2.2), 3.0, 0, 30)).thenReturn(newsitemsNearPetoneStationFirstPage)
    when(relatedTagsService.getRelatedTagsForLocation(any(), any(), any())).thenReturn(Seq())
    when(relatedTagsService.getRelatedPublishersForLocation(any(), any())).thenReturn(Seq())
    request.setPathInfo("/geotagged")
    request.setAttribute(LocationParameterFilter.LOCATION, validLocation)
    request.setAttribute(LocationParameterFilter.RADIUS, 3.0)

    val modelAndView = modelBuilder.populateContentModel(request).get

    assertEquals(newsitemsNearPetoneStationFirstPage, modelAndView.getModel.get("main_content"))
  }

  @Test
  @throws[Exception]
  def locationSearchesShouldHavePagination {
    request.setPathInfo("/geotagged")
    when(contentRetrievalService.getNewsitemsNearCount(new LatLong(1.1, 2.2), 1.0)).thenReturn(GeotaggedModelBuilderTest.LOCATION_RESULTS_COUNT)
    when(relatedTagsService.getRelatedTagsForLocation(any(), any(), any())).thenReturn(Seq())
    when(relatedTagsService.getRelatedPublishersForLocation(any(), any())).thenReturn(Seq())
    request.setAttribute(LocationParameterFilter.LOCATION, validLocation)

    val modelAndView = modelBuilder.populateContentModel(request).get

    assertEquals(1, modelAndView.getModel.get("page"))
    assertEquals(GeotaggedModelBuilderTest.LOCATION_RESULTS_COUNT, modelAndView.getModel.get("main_content_total"))
  }

  @Test
  @throws[Exception]
  def locationSearchesShouldHaveCorrectContentOnSecondPaginationPage {
    request.setPathInfo("/geotagged")
    when(contentRetrievalService.getNewsitemsNearCount(new LatLong(1.1, 2.2), 1.0)).thenReturn(GeotaggedModelBuilderTest.LOCATION_RESULTS_COUNT)
    when(contentRetrievalService.getNewsitemsNear(new LatLong(1.1, 2.2), 1.0, 30, 30)).thenReturn(newsitemsNearPetoneStationSecondPage)
    request.setAttribute(LocationParameterFilter.LOCATION, validLocation)
    request.setAttribute("page", 2)
    when(relatedTagsService.getRelatedTagsForLocation(any(), any(), any())).thenReturn(Seq())
    when(relatedTagsService.getRelatedPublishersForLocation(any(), any())).thenReturn(Seq())

    val modelAndView = modelBuilder.populateContentModel(request).get

    assertEquals(2, modelAndView.getModel.get("page"))
    assertEquals(newsitemsNearPetoneStationSecondPage, modelAndView.getModel.get("main_content"))
  }

  @Test
  @throws[Exception]
  def locationSearchShouldNotSetLocationWasInvalid {
    request.setPathInfo("/geotagged")
    when(invalidLocation.getLatLong).thenReturn(null)
    request.setAttribute(LocationParameterFilter.LOCATION, invalidLocation)

    val modelAndView = modelBuilder.populateContentModel(request).get

    assertNull(modelAndView.getModel.get("location"))
  }

}
