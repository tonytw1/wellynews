package nz.co.searchwellington.controllers.submission

import nz.co.searchwellington.geocoding.osm.GeoCodeService
import nz.co.searchwellington.model.{Geocode, OsmId}

trait GeotagParsing {

  def geocodeService: GeoCodeService

  def parseGeotag(address: String, osmId: String): Option[Geocode] = {
    if (osmId.nonEmpty) {
      val osm = parseOsmId(osmId)
      val commonOsm = new uk.co.eelpieconsulting.common.geo.model.OsmId(
        osm.id, uk.co.eelpieconsulting.common.geo.model.OsmType.valueOf(osm.`type`)
      )

      Option(geocodeService.resolveOsmId(commonOsm)).map { rp =>
        val resolvedLatLong = rp.getLatLong
        Geocode(address = Some(address), osmId = Some(osm),
          latitude = Some(resolvedLatLong.getLatitude),
          longitude = Some(resolvedLatLong.getLongitude))
      }
    } else {
      None
    }
  }

  def parseOsmId(osmIdString: String): OsmId = {
    val id = osmIdString.split("/")(0).toLong
    val `type` = osmIdString.split("/")(1)
    OsmId(id = id, `type` = `type`)
  }

  def osmToString(osmId: OsmId): String = {
    osmId.id + "/" + osmId.`type`
  }

}
