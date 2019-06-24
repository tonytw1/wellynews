package nz.co.searchwellington.model.frontend

case class Place(displayName: String, latLong: Option[LatLong], osmId: Option[OSMId])