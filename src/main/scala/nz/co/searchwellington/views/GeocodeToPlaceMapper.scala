package nz.co.searchwellington.views

import nz.co.searchwellington.model.Geocode
import nz.co.searchwellington.model.frontend.{LatLong, OSMId, Place}
import org.springframework.stereotype.Component

@Component class GeocodeToPlaceMapper {

  def mapGeocodeToPlace(geocode: Geocode): Place = {
    val latLong = geocode.latitude.flatMap { lat =>
      geocode.longitude.map { lon =>
        LatLong(lat, lon)
      }
    }

    val osmId = geocode.osmId.flatMap { oid =>
      geocode.osmType.map { ot =>
        OSMId(oid, ot)
      }
    }

    Place(geocode.getDisplayName, latLong, osmId)
  }

}