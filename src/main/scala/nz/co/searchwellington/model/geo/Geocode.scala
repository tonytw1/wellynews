package nz.co.searchwellington.model.geo

case class Geocode(address: Option[String] = None,
                   latitude: Option[Double] = None,
                   longitude: Option[Double] = None,
                   osmId: Option[OsmId] = None
                  ) {

  def getAddress: String = address.orNull

  def getLatitude: Double = latitude.getOrElse(0)

  def getLongitude: Double = longitude.getOrElse(0)

  def isValid: Boolean = latitude.nonEmpty && longitude.nonEmpty

  def getOsmId: OsmId = osmId.orNull

  def latLong: Option[uk.co.eelpieconsulting.common.geo.model.LatLong] = {
    for {
      lat <- latitude
      lon <- longitude
    } yield {
      new uk.co.eelpieconsulting.common.geo.model.LatLong(lat, lon)
    }
  }

  @deprecated def getOsmPlaceId: String = {
    osmId.map { osmId =>
      osmId.id + "/" + osmId.`type`
    }.orNull
  }

  def getDisplayName: String = {
    val positionLabel = for {
      lat <- latitude
      lon <- longitude
    } yield {
      lat + ", " + lon
    }
    val availableDisplayNames = Seq(address, positionLabel).flatten
    availableDisplayNames.headOption.orNull
  }

}
