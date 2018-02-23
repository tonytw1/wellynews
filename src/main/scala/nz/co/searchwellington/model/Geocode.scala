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
    return address
  }


  def getLatitude: Double = {
    return latitude
  }

  def getLongitude: Double = {
    return longitude
  }


  def isValid: Boolean = {
    return latitude != null && longitude != null
  }

  def getType: String = {
    return `type`
  }


  def getOsmId: Long = {
    return osmId
  }


  def getOsmType: String = {
    return osmType
  }


  @deprecated def getOsmPlaceId: String = {
    if (osmId != null && osmType != null) {
      return osmId + "/" + osmType
    }
    return null
  }

  def getResolver: String = {
    return resolver
  }


  override def toString: String = {
    return "Geocode [address=" + address + ", id=" + id + ", latitude=" + latitude + ", longitude=" + longitude + ", osmPlaceId=" + osmId + ", type=" + `type` + "]"
  }

  def getDisplayName: String = {
    if (address != null) {
      return address
    }
    return latitude + ", " + longitude
  }

}