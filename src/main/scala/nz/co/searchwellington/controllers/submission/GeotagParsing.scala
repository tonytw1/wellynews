package nz.co.searchwellington.controllers.submission

import nz.co.searchwellington.geocoding.osm.GeoCodeService
import nz.co.searchwellington.model.geo.{Geocode, LatLong, OsmId}
import uk.co.eelpieconsulting.common.geo.model.OsmType

import scala.concurrent.ExecutionContext

trait GeotagParsing {

  def geocodeService: GeoCodeService

  def parseGeotag(address: String, osmId: String)(implicit ec: ExecutionContext): Option[Geocode] = {
    if (osmId.nonEmpty) {
      parseOsmId(osmId).flatMap { osm: uk.co.eelpieconsulting.common.geo.model.OsmId =>
        val resolvedPlace = geocodeService.resolveOsmId(osm)
        Option(resolvedPlace).map { rp =>
          val resolvedLatLong = Option(rp.getLatLong).map { ll =>
            LatLong(ll.getLatitude, ll.getLongitude)
          }
          Geocode(
            address = Some(address),
            osmId = Some(OsmId(osm.getId, osm.getType.name())),
            latLong = resolvedLatLong
          )
        }
      }
    } else {
      None
    }
  }

  def parseOsmId(osmIdString: String): Option[uk.co.eelpieconsulting.common.geo.model.OsmId] = {
    val splits = osmIdString.split("/")
    if (splits.length == 2) {
      val id = splits(0).toLong
      val `type` = splits(1)

      val osmType = OsmType.values().toSeq.find { t =>
        t.name() == `type`
      }
      osmType.map { osmType =>
          new uk.co.eelpieconsulting.common.geo.model.OsmId(
          id, osmType
        )
      }
    } else {
      None
    }
  }

  def osmToString(osmId: OsmId): String = {
    osmId.id + "/" + osmId.`type`
  }

}
