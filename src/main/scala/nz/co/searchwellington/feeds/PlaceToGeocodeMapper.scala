package nz.co.searchwellington.feeds

import nz.co.searchwellington.feeds.whakaoko.model.Place
import nz.co.searchwellington.model.Geocode
import org.springframework.stereotype.Component

@Component class PlaceToGeocodeMapper {

  def mapPlaceToGeocode(p: Place): Geocode = {

    def composeAddress(place: Place): Option[String] = {
      place.latLong.map(ll => Seq(ll.latitude, ll.longitude).mkString(", "))
    }

    Geocode(
      address = composeAddress(p),
      latitude = p.latLong.map(_.latitude),
      longitude = p.latLong.map(_.longitude)
    )
  }

}
