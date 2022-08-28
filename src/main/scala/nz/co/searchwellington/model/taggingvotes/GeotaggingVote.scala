package nz.co.searchwellington.model.taggingvotes

import nz.co.searchwellington.model.geo.Geocode

case class GeotaggingVote(geocode: Geocode, explanation: String, weight: Int) {
  def getGeocode: Geocode = geocode
  def getExplanation: String = explanation
  override def toString = s"GeotaggingVote($geocode, $explanation, $weight)"
}


