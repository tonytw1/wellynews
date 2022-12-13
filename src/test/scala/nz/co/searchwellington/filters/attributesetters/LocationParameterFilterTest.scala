package nz.co.searchwellington.filters.attributesetters

import nz.co.searchwellington.geocoding.osm.{GeoCodeService, OsmIdParser}
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.mock.web.MockHttpServletRequest
import uk.co.eelpieconsulting.common.geo.model.{LatLong, OsmId, OsmType, Place}

object LocationParameterFilterTest {
  private val VALID_LOCATION = "Petone Station"
}

class LocationParameterFilterTest {
  final private val geocodeService = Mockito.mock(classOf[GeoCodeService])
  final private val petoneStation = new Place(LocationParameterFilterTest.VALID_LOCATION, new LatLong(1.1, 2.2), new OsmId(123, OsmType.NODE))
  final private val filter = new LocationParameterFilter(geocodeService, new OsmIdParser)

  @Test def canResolveOsmIdAsLocation() = {
    val request = new MockHttpServletRequest
    request.setParameter("osm", "123/NODE")
    Mockito.when(geocodeService.resolveOsmId(petoneStation.getOsmId)).thenReturn(petoneStation)
    filter.filter(request)
    val locationAttribute = request.getAttribute("location").asInstanceOf[Place]
    assertEquals(LocationParameterFilterTest.VALID_LOCATION, locationAttribute.getAddress)
  }

  @Test def canResolveLocationSearchRadius() = {
    val request = new MockHttpServletRequest
    request.setParameter("radius", "3")
    filter.filter(request)
    assertEquals(3.0, request.getAttribute("radius"))
  }

  @Test def canResolveAbsoluteLatLongPointAsALocation() = {
    val request = new MockHttpServletRequest
    request.setParameter("latitude", "51.2")
    request.setParameter("longitude", "-0.1")
    filter.filter(request)
    val locationAttribute = request.getAttribute("location").asInstanceOf[Place]
    assertEquals(51.2, locationAttribute.getLatLong.getLatitude, 0)
    assertEquals(-0.1, locationAttribute.getLatLong.getLongitude, 0)
  }

}