package nz.co.searchwellington.feeds

import nz.co.searchwellington.feeds.whakaoko.model.Place
import nz.co.searchwellington.model.geo.Geocode
import org.springframework.stereotype.Component

@Component class PlaceToGeocodeMapper {

  def mapPlaceToGeocode(p: Place): Geocode = {
    Geocode(
      latitude = p.latLong.map(_.latitude),
      longitude = p.latLong.map(_.longitude)
    )
  }

}
