package nz.co.searchwellington.model.frontend

case class Place(displayName: String, latLong: Option[LatLong] = None, osmId: Option[OSMId] = None) {

  def getDisplayName: String = displayName
  def getLatLong: LatLong = latLong.orNull
  def getOSMId: OSMId = osmId.orNull

}