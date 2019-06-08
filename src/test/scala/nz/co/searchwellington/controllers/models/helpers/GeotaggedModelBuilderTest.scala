package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.controllers.{LoggedInUserFilter, RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.filters.LocationParameterFilter
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertEquals, assertNull, assertTrue}
import org.junit.Test
import org.mockito.Matchers._
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import uk.co.eelpieconsulting.common.geo.model.{LatLong, Place}

import scala.concurrent.Future

class GeotaggedModelBuilderTest {
  val contentRetrievalService = mock(classOf[ContentRetrievalService])
  val urlBuilder = mock(classOf[UrlBuilder])
  val rssUrlBuilder = mock(classOf[RssUrlBuilder])
  val relatedTagsService = mock(classOf[RelatedTagsService])
  val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])
  val loggedInUserFilter = mock(classOf[LoggedInUserFilter])
  val newsitemsNearPetoneStationFirstPage: Seq[FrontendResource] = null
  val newsitemsNearPetoneStationSecondPage: Seq[FrontendResource] = null

  val request = new MockHttpServletRequest
  val validLocation = new Place("Petone Station", new LatLong(1.1, 2.2), null)
  val invalidLocation = new Place("No where", null, null)

  private val TOTAL_GEOTAGGED_COUNT = 512L
  private val LOCATION_RESULTS_COUNT= 33L

  private val loggedInUser = None

  val modelBuilder = new GeotaggedModelBuilder(contentRetrievalService, urlBuilder, rssUrlBuilder, relatedTagsService, commonAttributesModelBuilder, loggedInUserFilter)

  @Test
  def shouldBeValidForGeotaggedPath {
    request.setPathInfo("/geotagged")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def shouldBeValidForGeotaggedRssPath {
    request.setPathInfo("/geotagged/rss")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def shouldBeValidForGeotaggedJSONPath {
    request.setPathInfo("/geotagged/json")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def geotaggedNewsitemsPageShouldHavePaginationInformation {
    request.setPathInfo("/geotagged")
    when(contentRetrievalService.getGeocodedNewitemsCount(loggedInUser)).thenReturn(TOTAL_GEOTAGGED_COUNT)
    when(contentRetrievalService.getGeocodedNewsitems(0, 30, loggedInUser)).thenReturn(Future.successful(Seq.empty))

    val modelAndView = modelBuilder.populateContentModel(request).get

    assertEquals(1, modelAndView.getModel.get("page"))
    assertEquals(TOTAL_GEOTAGGED_COUNT, modelAndView.getModel.get("main_content_total"))
  }

  @Test
  def locationSearchesShouldHaveNearbyNewsitemsAsTheMainContent {
    when(contentRetrievalService.getNewsitemsNear(new LatLong(1.1, 2.2), 1.0, 0, 30, loggedInUser)).thenReturn(newsitemsNearPetoneStationFirstPage)
    when(relatedTagsService.getRelatedTagsForLocation(any(), any(), any())).thenReturn(Future.successful(Seq()))
    when(relatedTagsService.getRelatedPublishersForLocation(any(), any(), any())).thenReturn(Future.successful(Seq()))
    request.setPathInfo("/geotagged")
    request.setAttribute(LocationParameterFilter.LOCATION, validLocation)

    val modelAndView = modelBuilder.populateContentModel(request).get

    assertEquals(newsitemsNearPetoneStationFirstPage, modelAndView.getModel.get("main_content"))
  }

  @Test
  def locationSearchRadiusShouldBeTweakableFromTheRequestParameters {
    when(contentRetrievalService.getNewsitemsNear(new LatLong(1.1, 2.2), 3.0, 0, 30, loggedInUser)).thenReturn(newsitemsNearPetoneStationFirstPage)
    when(relatedTagsService.getRelatedTagsForLocation(any(), any(), any())).thenReturn(Future.successful(Seq()))
    when(relatedTagsService.getRelatedPublishersForLocation(any(), any(), any())).thenReturn(Future.successful(Seq()))
    request.setPathInfo("/geotagged")
    request.setAttribute(LocationParameterFilter.LOCATION, validLocation)
    request.setAttribute(LocationParameterFilter.RADIUS, 3.0)

    val modelAndView = modelBuilder.populateContentModel(request).get

    assertEquals(newsitemsNearPetoneStationFirstPage, modelAndView.getModel.get("main_content"))
  }

  @Test
  def locationSearchesShouldHavePagination {
    request.setPathInfo("/geotagged")
    when(contentRetrievalService.getNewsitemsNearCount(new LatLong(1.1, 2.2), 1.0, loggedInUser)).thenReturn(LOCATION_RESULTS_COUNT)
    when(relatedTagsService.getRelatedTagsForLocation(any(), any(), any())).thenReturn(Future.successful(Seq()))
    when(relatedTagsService.getRelatedPublishersForLocation(any(), any(), any())).thenReturn(Future.successful(Seq()))
    request.setAttribute(LocationParameterFilter.LOCATION, validLocation)

    val modelAndView = modelBuilder.populateContentModel(request).get

    assertEquals(1, modelAndView.getModel.get("page"))
    assertEquals(LOCATION_RESULTS_COUNT, modelAndView.getModel.get("main_content_total"))
  }

  @Test
  def locationSearchesShouldHaveCorrectContentOnSecondPaginationPage {
    request.setPathInfo("/geotagged")
    when(contentRetrievalService.getNewsitemsNearCount(new LatLong(1.1, 2.2), 1.0, loggedInUser)).thenReturn(LOCATION_RESULTS_COUNT)
    when(contentRetrievalService.getNewsitemsNear(new LatLong(1.1, 2.2), 1.0, 30, 30, loggedInUser)).thenReturn(newsitemsNearPetoneStationSecondPage)
    request.setAttribute(LocationParameterFilter.LOCATION, validLocation)
    request.setAttribute("page", 2)
    when(relatedTagsService.getRelatedTagsForLocation(any(), any(), any())).thenReturn(Future.successful(Seq()))
    when(relatedTagsService.getRelatedPublishersForLocation(any(), any(), any())).thenReturn(Future.successful(Seq()))

    val modelAndView = modelBuilder.populateContentModel(request).get

    assertEquals(2, modelAndView.getModel.get("page"))
    assertEquals(newsitemsNearPetoneStationSecondPage, modelAndView.getModel.get("main_content"))
  }

  @Test
  def locationSearchShouldNotSetLocationWasInvalid {
    request.setPathInfo("/geotagged")
    request.setAttribute(LocationParameterFilter.LOCATION, invalidLocation)
    when(contentRetrievalService.getGeocodedNewsitems(0, 30, loggedInUser)).thenReturn(Future.successful(Seq.empty))

    val modelAndView = modelBuilder.populateContentModel(request).get

    assertNull(modelAndView.getModel.get("location"))
  }

}
