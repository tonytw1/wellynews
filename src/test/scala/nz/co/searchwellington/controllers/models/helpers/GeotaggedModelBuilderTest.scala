package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.filters.LocationParameterFilter
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.tagging.RelatedTagsService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Assert.{assertEquals, assertNull, assertTrue}
import org.junit.Test
import org.mockito.Matchers._
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import uk.co.eelpieconsulting.common.geo.model.{LatLong, Place}

import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

class GeotaggedModelBuilderTest extends ReasonableWaits with ContentFields {
  val contentRetrievalService = mock(classOf[ContentRetrievalService])
  val urlBuilder = mock(classOf[UrlBuilder])
  val rssUrlBuilder = mock(classOf[RssUrlBuilder])
  val relatedTagsService = mock(classOf[RelatedTagsService])
  val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])
  val newsitemsNearPetoneStationFirstPage: Seq[FrontendResource] = Seq.empty  // TODO Not realistic
  val newsitemsNearPetoneStationSecondPage: Seq[FrontendResource] = null

  val request = new MockHttpServletRequest
  val validLocation = new Place("Petone Station", new LatLong(1.1, 2.2), null)
  val invalidLocation = new Place("No where", null, null)

  private val TOTAL_GEOTAGGED_COUNT = 512L
  private val LOCATION_RESULTS_COUNT= 33L

  private val loggedInUser = None

  val modelBuilder = new GeotaggedModelBuilder(contentRetrievalService, urlBuilder, rssUrlBuilder, relatedTagsService, commonAttributesModelBuilder)

  @Test
  def shouldBeValidForGeotaggedPath {
    request.setRequestURI("/geotagged")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def shouldBeValidForGeotaggedRssPath {
    request.setRequestURI("/geotagged/rss")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def shouldBeValidForGeotaggedJSONPath {
    request.setRequestURI("/geotagged/json")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def geotaggedNewsitemsPageShouldHavePaginationInformation {
    request.setRequestURI("/geotagged")
    when(contentRetrievalService.getGeocodedNewitemsCount(loggedInUser)).thenReturn(Future.successful(TOTAL_GEOTAGGED_COUNT))
    when(contentRetrievalService.getGeocodedNewsitems(0, 30, loggedInUser)).thenReturn(Future.successful(Seq.empty))

    val modelAndView = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(1, modelAndView.getModel.get("page"))
    assertEquals(TOTAL_GEOTAGGED_COUNT, modelAndView.getModel.get("main_content_total"))
  }

  @Test
  def locationSearchesShouldHaveNearbyNewsitemsAsTheMainContent {
    when(contentRetrievalService.getNewsitemsNear(new LatLong(1.1, 2.2), 1.0, 0, 30, loggedInUser)).
      thenReturn(Future.successful(newsitemsNearPetoneStationFirstPage))
    when(contentRetrievalService.getNewsitemsNearCount(new LatLong(1.1, 2.2), 1.0, loggedInUser)).
      thenReturn(Future.successful(newsitemsNearPetoneStationFirstPage.size.toLong))
    when(relatedTagsService.getRelatedTagsForLocation(any(), any(), any())).thenReturn(Future.successful(Seq()))
    when(relatedTagsService.getRelatedPublishersForLocation(any(), any(), any())).thenReturn(Future.successful(Seq()))
    request.setRequestURI("/geotagged")
    request.setAttribute(LocationParameterFilter.LOCATION, validLocation)

    val modelAndView = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(newsitemsNearPetoneStationFirstPage.asJava, modelAndView.getModel.get(MAIN_CONTENT))
  }

  @Test
  def locationSearchRadiusShouldBeTweakableFromTheRequestParameters {
    when(contentRetrievalService.getNewsitemsNear(new LatLong(1.1, 2.2), 3.0, 0, 30, loggedInUser)).
      thenReturn(Future.successful(newsitemsNearPetoneStationFirstPage))
    when(contentRetrievalService.getNewsitemsNearCount(new LatLong(1.1, 2.2), 3.0, loggedInUser)).
      thenReturn(Future.successful(newsitemsNearPetoneStationFirstPage.size.toLong))
    when(relatedTagsService.getRelatedTagsForLocation(any(), any(), any())).thenReturn(Future.successful(Seq()))
    when(relatedTagsService.getRelatedPublishersForLocation(any(), any(), any())).thenReturn(Future.successful(Seq()))
    request.setRequestURI("/geotagged")
    request.setAttribute(LocationParameterFilter.LOCATION, validLocation)
    request.setAttribute(LocationParameterFilter.RADIUS, 3.0)

    val modelAndView = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get
    assertEquals(newsitemsNearPetoneStationFirstPage.asJava, modelAndView.getModel.get(MAIN_CONTENT))
  }

  @Test
  def locationSearchesShouldHavePagination {
    request.setRequestURI("/geotagged")
    when(contentRetrievalService.getNewsitemsNear(new LatLong(1.1, 2.2), 1.0, 0, 30, loggedInUser)).
      thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getNewsitemsNearCount(new LatLong(1.1, 2.2), 1.0, loggedInUser)).
      thenReturn(Future.successful(LOCATION_RESULTS_COUNT))
    when(relatedTagsService.getRelatedTagsForLocation(any(), any(), any())).thenReturn(Future.successful(Seq()))
    when(relatedTagsService.getRelatedPublishersForLocation(any(), any(), any())).thenReturn(Future.successful(Seq()))
    request.setAttribute(LocationParameterFilter.LOCATION, validLocation)

    val modelAndView = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(1, modelAndView.getModel.get("page"))
    assertEquals(LOCATION_RESULTS_COUNT, modelAndView.getModel.get("main_content_total"))
  }

  @Test
  def locationSearchesShouldHaveCorrectContentOnSecondPaginationPage {
    request.setRequestURI("/geotagged")
    when(contentRetrievalService.getNewsitemsNearCount(new LatLong(1.1, 2.2), 1.0, loggedInUser)).
      thenReturn(Future.successful(LOCATION_RESULTS_COUNT))
    when(contentRetrievalService.getNewsitemsNear(new LatLong(1.1, 2.2), 1.0, 30, 30, loggedInUser)).
      thenReturn(Future.successful(newsitemsNearPetoneStationSecondPage))
    request.setAttribute(LocationParameterFilter.LOCATION, validLocation)
    request.setAttribute("page", 2)
    when(relatedTagsService.getRelatedTagsForLocation(any(), any(), any())).thenReturn(Future.successful(Seq()))
    when(relatedTagsService.getRelatedPublishersForLocation(any(), any(), any())).thenReturn(Future.successful(Seq()))

    val modelAndView = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(2, modelAndView.getModel.get("page"))
    assertEquals(newsitemsNearPetoneStationSecondPage, modelAndView.getModel.get(MAIN_CONTENT))
  }

  @Test
  def locationSearchShouldNotSetLocationWasInvalid {
    request.setRequestURI("/geotagged")
    request.setAttribute(LocationParameterFilter.LOCATION, invalidLocation)
    when(contentRetrievalService.getGeocodedNewsitems(0, 30, loggedInUser)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getGeocodedNewitemsCount(loggedInUser)).thenReturn(Future.successful(0L))

    val modelAndView = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertNull(modelAndView.getModel.get("location"))
  }

}
