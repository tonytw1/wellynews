package nz.co.searchwellington.geocoding.osm

import uk.co.eelpieconsulting.common.geo.model.{OsmId, Place}

import scala.concurrent.Future

trait GeoCodeService {
  def resolveOsmId(osmId: OsmId): Future[Option[Place]]
}