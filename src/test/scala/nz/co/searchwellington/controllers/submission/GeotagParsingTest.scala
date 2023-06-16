package nz.co.searchwellington.controllers.submission

import nz.co.searchwellington.geocoding.osm.GeoCodeService
import nz.co.searchwellington.model.geo.{Geocode, LatLong, OsmId}
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, when}
import uk.co.eelpieconsulting.common.geo.model
import uk.co.eelpieconsulting.common.geo.model.{OsmType, Place}

import scala.concurrent.Future

class GeotagParsingTest extends GeotagParsing {

  val geocodeService: GeoCodeService = mock(classOf[GeoCodeService])

  @Test
  def shouldParseStringOsmIdToOsmId(): Unit = {
    assertEquals(Some(new uk.co.eelpieconsulting.common.geo.model.OsmId(123L, OsmType.NODE)), parseOsmId("123/NODE")) // TODO why is this not an enum?
  }

  @Test
  def shouldReturnNoneForInvalidFormats(): Unit = {
    assertTrue(parseOsmId("123/APPLE").isEmpty)
    assertTrue(parseOsmId("123ABC").isEmpty)
  }

  @Test
  def shouldParseUserInputToPlace(): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    when(geocodeService.resolveOsmId(new model.OsmId(456L, OsmType.RELATION))).thenReturn(
      Future.successful(Some(new Place(null, new uk.co.eelpieconsulting.common.geo.model.LatLong(51.0, -0.3), null)))
    )

    val result = parseGeotag("Somewhere", "456/RELATION")

    assertEquals(Some(Geocode(address = Some("Somewhere"), Some(LatLong(51.0, -0.3)), osmId = Some(OsmId(456L, "RELATION")))), result)
  }
}
