package nz.co.searchwellington.model.taggingvotes

import nz.co.searchwellington.model.Geocode

class GeotaggingVote(val geocode: Geocode, val explanation: String, val weight: Int) {
  def getGeocode: Geocode = geocode
  def getExplanation: String = explanation
}





