package nz.co.searchwellington.geocoding.osm

import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.model.OsmId
import uk.co.eelpieconsulting.common.geo.model.OsmType

@Component class OsmIdParser {
  def parseOsmId(osm: String): OsmId = {
    val split = osm.split("/")
    if (split.length == 2) {
      val idString = split(0)
      val typeString = split(1)
      return new OsmId(idString.toLong, OsmType.valueOf(typeString)) // TODO catch
    }
    null  // TODO option
  }
}