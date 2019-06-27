package nz.co.searchwellington.model.frontend

case class LatLong(latitude: Double, longitude: Double) {

  def getLatitude: Double = latitude
  def getLongitude: Double = longitude

}