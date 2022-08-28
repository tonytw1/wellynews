package nz.co.searchwellington.model.geo

case class Geocode(address: Option[String] = None,
                   latLong: Option[LatLong] = None,
                   osmId: Option[OsmId] = None
                  ) {

  def getAddress: String = address.orNull

  def isValid: Boolean = latLong.nonEmpty

  def getOsmId: OsmId = osmId.orNull

  @deprecated def getOsmPlaceId: String = {
    osmId.map { osmId =>
      osmId.id + "/" + osmId.`type`
    }.orNull
  }

  def getDisplayName: String = {
    val positionLabel = latLong.map { ll =>
      ll.latitude + ", " + ll.longitude
    }
    val availableDisplayNames = Seq(address, positionLabel).flatten
    availableDisplayNames.headOption.orNull
  }

  def getLatLong: LatLong = latLong.orNull

}
