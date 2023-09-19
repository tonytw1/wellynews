package nz.co.searchwellington.gecoding.osm

import nz.co.searchwellington.geocoding.osm.OsmIdParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.co.eelpieconsulting.common.geo.model.OsmType

class OsmIdParserTest {

  val parser: OsmIdParser = new OsmIdParser()

  @Test
  def canParseIdSlashTypeStyleOsmIdString() = {
    val parsedOsmId = parser.parseOsmId("123456/WAY")

    assertEquals(123456L, parsedOsmId.get.getId)
    assertEquals(OsmType.WAY, parsedOsmId.get.getType)
  }

  @Test
  def canAccommodateVaryingLengthsOfTheOsmTypes() {
    val parsedOsmId = parser.parseOsmId("123456/RELATION")

    assertEquals(OsmType.RELATION, parsedOsmId.get.getType)
  }

  @Test
  def canAccommodateVaryingLengthsOfTheOsmIds() {
    val parsedOsmId = parser.parseOsmId("123/WAY")

    assertEquals(123L, parsedOsmId.get.getId)
  }

}
