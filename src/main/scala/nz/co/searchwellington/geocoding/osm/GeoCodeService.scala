package nz.co.searchwellington.geocoding.osm

import uk.co.eelpieconsulting.common.geo.model.{OsmId, Place}

trait GeoCodeService {
  def resolveOsmId(osmId: OsmId): Place
}