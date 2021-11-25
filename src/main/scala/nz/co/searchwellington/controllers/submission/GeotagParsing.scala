package nz.co.searchwellington.controllers.submission

import nz.co.searchwellington.geocoding.osm.GeoCodeService
import nz.co.searchwellington.model.{Geocode, OsmId}
import uk.co.eelpieconsulting.common.geo.model.OsmType

trait GeotagParsing {

  def geocodeService: GeoCodeService

  def parseGeotag(address: String, osmId: String): Option[Geocode] = {
    if (osmId.nonEmpty) {
      val osm = parseOsmId(osmId).get // TODO naked get
      val commonOsm = new uk.co.eelpieconsulting.common.geo.model.OsmId(
        osm.id, uk.co.eelpieconsulting.common.geo.model.OsmType.valueOf(osm.`type`)
      )

      Option(geocodeService.resolveOsmId(commonOsm)).map { rp =>
        val resolvedLatLong = rp.getLatLong
        Geocode(
          address = Some(address),
          osmId = Some(osm),
          latitude = Some(resolvedLatLong.getLatitude),
          longitude = Some(resolvedLatLong.getLongitude)
        )
      }
    } else {
      None
    }
  }

  def parseOsmId(osmIdString: String): Option[OsmId] = {
    val splits = osmIdString.split("/")
    if (splits.length == 2) {
      val id = splits(0).toLong
      val `type` = splits(1)

      val osmType = OsmType.values().toSeq.find { t =>
        t.name().take(1) == `type`
      }
      osmType.map { osmType =>
        OsmId(id = id, `type` = osmType.name().take(1))
      }
    } else {
      None
    }
  }

  def osmToString(osmId: OsmId): String = {
    osmId.id + "/" + osmId.`type`
  }

}
