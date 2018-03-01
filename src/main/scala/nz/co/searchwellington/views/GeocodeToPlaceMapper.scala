package nz.co.searchwellington.views

import nz.co.searchwellington.model.Geocode
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.model.LatLong
import uk.co.eelpieconsulting.common.geo.model.OsmId
import uk.co.eelpieconsulting.common.geo.model.OsmType
import uk.co.eelpieconsulting.common.geo.model.Place

@Component class GeocodeToPlaceMapper {

  def mapGeocodeToPlace(geocode: Geocode): Place = {
    val latLong = geocode.latitude.flatMap { lat =>
      geocode.longitude.map { lon =>
        new LatLong(lat, lon)
      }
    }

    val osmId = geocode.osmId.flatMap { oid =>
      geocode.osmType.map { ot =>
        new OsmId(oid, OsmType.valueOf(ot))
      }
    }

    new Place(geocode.getDisplayName, latLong.getOrElse(null), osmId.getOrElse(null))
  }

}