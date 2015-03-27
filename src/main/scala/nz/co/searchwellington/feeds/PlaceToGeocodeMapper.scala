package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.Geocode
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.model.Place

@Component class PlaceToGeocodeMapper {

  def mapPlaceToGeocode(place: Place): Geocode = {
    val address: String = composeAddress(place)
    val geocode: Geocode = new Geocode(address,
        if (place.getLatLong != null) place.getLatLong.getLatitude else null,
        if (place.getLatLong != null) place.getLatLong.getLongitude else null,
        if (place.getOsmId != null) place.getOsmId.getId else null,
        if (place.getOsmId != null) place.getOsmId.getType.toString else null)
    geocode
  }

  private def composeAddress(place: Place): String = {
    var address: String = place.getAddress
    if (address == null && place.getLatLong != null) {
      address = place.getLatLong.getLatitude + ", " + place.getLatLong.getLongitude
    }
    address
  }

}
