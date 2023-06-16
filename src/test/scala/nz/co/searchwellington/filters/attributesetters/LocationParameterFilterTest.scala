package nz.co.searchwellington.filters.attributesetters

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.geocoding.osm.{GeoCodeService, OsmIdParser}
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.springframework.mock.web.MockHttpServletRequest
import uk.co.eelpieconsulting.common.geo.model.{LatLong, OsmId, OsmType, Place}

import scala.concurrent.{Await, Future}

class LocationParameterFilterTest extends ReasonableWaits {
  private val VALID_LOCATION_NAME = "Petone Station"
  private val geocodeService = Mockito.mock(classOf[GeoCodeService])
  private val petoneStation = new Place(VALID_LOCATION_NAME, new LatLong(1.1, 2.2), new OsmId(123, OsmType.NODE))

  private val filter = new LocationParameterFilter(geocodeService, new OsmIdParser)

  @Test
  def canResolveOsmIdAsLocation() = {
    val request = new MockHttpServletRequest
    request.setParameter("osm", "123/NODE")
    when(geocodeService.resolveOsmId(petoneStation.getOsmId)).thenReturn(Future.successful(Some(petoneStation)))

    val attributes = Await.result(filter.setAttributes(request), TenSeconds)

    val locationAttribute = attributes("location").asInstanceOf[Place]
    assertEquals(VALID_LOCATION_NAME, locationAttribute.getAddress)
  }

  @Test
  def canResolveLocationSearchRadius() = {
    val request = new MockHttpServletRequest
    request.setParameter("radius", "3")

    val attributes = Await.result(filter.setAttributes(request), TenSeconds)

    assertEquals(3.0, attributes("radius"))
  }

  @Test
  def canResolveAbsoluteLatLongPointAsALocation() = {
    val request = new MockHttpServletRequest
    request.setParameter("latitude", "51.2")
    request.setParameter("longitude", "-0.1")

    val attributes = Await.result(filter.setAttributes(request), TenSeconds)

    val locationAttribute = attributes("location").asInstanceOf[Place]
    assertEquals(51.2, locationAttribute.getLatLong.getLatitude, 0)
    assertEquals(-0.1, locationAttribute.getLatLong.getLongitude, 0)
  }

}