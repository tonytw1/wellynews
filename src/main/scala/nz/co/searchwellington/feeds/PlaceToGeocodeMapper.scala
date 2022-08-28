package nz.co.searchwellington.feeds

import nz.co.searchwellington.feeds.whakaoko.model.Place
import nz.co.searchwellington.model.geo.{Geocode, LatLong}
import org.springframework.stereotype.Component

@Component class PlaceToGeocodeMapper {

  def mapPlaceToGeocode(p: Place): Geocode = {
    val latLong = p.latLong.map { ll =>
      LatLong(ll.latitude, ll.longitude)
    }
    Geocode(latLong = latLong)
  }

}
