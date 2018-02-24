package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.Geocode
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.model.Place

@Component class PlaceToGeocodeMapper {

  def mapPlaceToGeocode(p: Place): Geocode = {

    def composeAddress(place: Place): String = {
      var address: String = place.getAddress
      if (address == null && place.getLatLong != null) {
        address = place.getLatLong.getLatitude + ", " + place.getLatLong.getLongitude
      }
      address
    }

    Geocode(
      address = Some(composeAddress(p)),
      latitude = Option(p.getLatLong).map(ll => ll.getLatitude),
      longitude = Option(p.getLatLong).map(ll => ll.getLongitude),
      osmId = Option(p.getOsmId).map(o => o.getId),
      osmType = Option(p.getOsmId).map(o => o.getType.toString)
    )
  }

}
