package nz.co.searchwellington.controllers

import nz.co.searchwellington.geocoding.osm.CachingNominatimResolveOsmIdService
import nz.co.searchwellington.model.{Geocode, OsmId}

trait GeotagParsing {

  def cachingNominatimResolveOsmIdService: CachingNominatimResolveOsmIdService

  def parseGeotag(address: String, osmId: String): Option[Geocode] = {
    if (osmId.nonEmpty) {
      val id = osmId.split("/")(0).toLong // TODO push to OSM object
      val `type` = osmId.split("/")(1)
      val osm = OsmId(id = id, `type` = `type`)

      val commonOsm = new uk.co.eelpieconsulting.common.geo.model.OsmId(
        osm.id, uk.co.eelpieconsulting.common.geo.model.OsmType.valueOf(osm.`type`)
      )
      val resolvedPlace = cachingNominatimResolveOsmIdService.callService(commonOsm)
      val resolvedLatLong = resolvedPlace.getLatLong
      Some(Geocode(address = Some(address), osmId = Some(osm),
        latitude = Some(resolvedLatLong.getLatitude),
        longitude = Some(resolvedLatLong.getLongitude)
      ))
    } else {
      None
    }
  }
}
