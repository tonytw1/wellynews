package nz.co.searchwellington.controllers.submission

import nz.co.searchwellington.geocoding.osm.GeoCodeService
import nz.co.searchwellington.model.OsmId
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock

class GeotagParsingTest extends GeotagParsing {

  val geocodeService: GeoCodeService = mock(classOf[GeoCodeService])

  @Test
  def shouldParseStringOsmIdToOsmId(): Unit = {
    val maybeId = parseOsmId("123/N")

    assertEquals(Some(OsmId(123L, "N")), maybeId) // TODO why is this not an enum?
  }

}
