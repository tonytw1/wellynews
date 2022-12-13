package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.filters.attributesetters.LocationParameterFilter
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.tagging.RelatedTagsService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.jupiter.api.Assertions.{assertEquals, assertNull, assertTrue}
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import uk.co.eelpieconsulting.common.geo.model.{LatLong, OsmId, OsmType, Place}

import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._
import scala.concurrent.ExecutionContext.Implicits.global

class GeotaggedModelBuilderTest extends ReasonableWaits with ContentFields {
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val urlBuilder = mock(classOf[UrlBuilder])
  private val rssUrlBuilder = mock(classOf[RssUrlBuilder])
  private val relatedTagsService = mock(classOf[RelatedTagsService])
  private val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])

  private val newsitemsNearPetoneStationFirstPage: Seq[FrontendResource] = Seq.empty  // TODO Not realistic
  private val newsitemsNearPetoneStationSecondPage: Seq[FrontendResource] = Seq.empty

  private val request = new MockHttpServletRequest
  private val validLocation = new Place("Petone Station", new LatLong(1.1, 2.2), new OsmId(123, OsmType.NODE))
  private val invalidLocation = new Place("No where", null, null)

  private val TOTAL_GEOTAGGED_COUNT = 512L
  private val LOCATION_RESULTS_COUNT= 33L

  private val loggedInUser = None

  private implicit val currentSpan: Span = Span.current()

  private val modelBuilder = new GeotaggedModelBuilder(contentRetrievalService, urlBuilder, rssUrlBuilder, relatedTagsService, commonAttributesModelBuilder)

  @Test
  def shouldBeValidForGeotaggedPath(): Unit = {
    request.setRequestURI("/geotagged")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def shouldBeValidForGeotaggedRssPath(): Unit = {
    request.setRequestURI("/geotagged/rss")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def shouldBeValidForGeotaggedJSONPath(): Unit = {
    request.setRequestURI("/geotagged/json")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def locationSearchesShouldHaveNearbyNewsitemsAsTheMainContent(): Unit = {
    when(contentRetrievalService.getNewsitemsNear(new LatLong(1.1, 2.2), 1.0, 30, loggedInUser)).
      thenReturn(Future.successful((newsitemsNearPetoneStationFirstPage, LOCATION_RESULTS_COUNT)))
    when(relatedTagsService.getRelatedTagsForLocation(any(), any(), any())(any(), any())).thenReturn(Future.successful(Seq()))
    when(relatedTagsService.getRelatedPublishersForLocation(any(), any(), any())(any(),any())).thenReturn(Future.successful(Seq()))
    request.setRequestURI("/geotagged")
    request.setAttribute(LocationParameterFilter.LOCATION, validLocation)

    val modelAndView = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(newsitemsNearPetoneStationFirstPage.asJava, modelAndView.get(MAIN_CONTENT))

    // User selected location should be available to the view
    assertEquals("Petone Station", modelAndView.get("location").asInstanceOf[Place].getAddress)
    assertEquals(new OsmId(123, OsmType.NODE), modelAndView.get("location").asInstanceOf[Place].getOsmId)
  }

  @Test
  def locationSearchRadiusShouldBeTweakableFromTheRequestParameters(): Unit = {
    when(contentRetrievalService.getNewsitemsNear(new LatLong(1.1, 2.2), 3.0, 30, loggedInUser)).
      thenReturn(Future.successful((newsitemsNearPetoneStationFirstPage, LOCATION_RESULTS_COUNT)))
    when(relatedTagsService.getRelatedTagsForLocation(any(), any(), any())(any(), any())).thenReturn(Future.successful(Seq()))
    when(relatedTagsService.getRelatedPublishersForLocation(any(), any(), any())(any(), any())).thenReturn(Future.successful(Seq()))
    request.setRequestURI("/geotagged")
    request.setAttribute(LocationParameterFilter.LOCATION, validLocation)
    request.setAttribute(LocationParameterFilter.RADIUS, 3.0)

    val modelAndView = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(newsitemsNearPetoneStationFirstPage.asJava, modelAndView.get(MAIN_CONTENT))
  }

  @Test
  def locationSearchShouldNotSetLocationIfLocationWasInvalid(): Unit = {
    request.setRequestURI("/geotagged")
    request.setAttribute(LocationParameterFilter.LOCATION, invalidLocation)
    when(contentRetrievalService.getGeocodedNewsitems(30, loggedInUser)).thenReturn(Future.successful((Seq.empty, LOCATION_RESULTS_COUNT)))

    val modelAndView = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertNull(modelAndView.get("location"))
  }

}
