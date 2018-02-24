package nz.co.searchwellington.model

case class Geocode(id: Int = 0,
                   address: Option[String] = None,
                   latitude: Option[Double] = None,
                   longitude: Option[Double] = None,
                   `type`: Option[String] = None,
                   osmId: Option[Long] = None,
                   osmType: Option[String] = None,
                   resolver: Option[String] = None) {

  def getId: Int = {
    return id
  }

  def getAddress: String = {
    address.getOrElse("")
  }


  def getLatitude: Double = {
    latitude.getOrElse(0)
  }

  def getLongitude: Double = {
    longitude.getOrElse(0)
  }

  def isValid: Boolean = {
    return latitude != null && longitude != null
  }

  def getType: String = {
    `type`.getOrElse(null)
  }


  def getOsmId: Long = {
    osmId.getOrElse(0)
  }


  def getOsmType: String = {
    osmType.getOrElse("")
  }

  @deprecated def getOsmPlaceId: String = {
    if (osmId != null && osmType != null) {
      return osmId + "/" + osmType
    }
    return null
  }

  def getResolver: String = {
    resolver.getOrElse(null)
  }

  def getDisplayName: String = {
    address.getOrElse {
      latitude + ", " + longitude
    }
  }

  override def toString: String = {
    return "Geocode [address=" + address + ", id=" + id + ", latitude=" + latitude + ", longitude=" + longitude + ", osmPlaceId=" + osmId + ", type=" + `type` + "]"
  }

}