package nz.co.searchwellington.controllers.submission

import nz.co.searchwellington.geocoding.osm.GeoCodeService
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import uk.co.eelpieconsulting.common.geo.model.OsmType

class GeotagParsingTest extends GeotagParsing {

  val geocodeService: GeoCodeService = mock(classOf[GeoCodeService])

  @Test
  def shouldParseStringOsmIdToOsmId(): Unit = {
    assertEquals(Some(new uk.co.eelpieconsulting.common.geo.model.OsmId(123L, OsmType.NODE)), parseOsmId("123/N")) // TODO why is this not an enum?
    assertEquals(None, parseOsmId("123/A"))
  }

}
