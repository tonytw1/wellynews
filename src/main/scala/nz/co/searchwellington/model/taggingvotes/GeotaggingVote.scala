package nz.co.searchwellington.model.taggingvotes

import nz.co.searchwellington.model.Geocode
import nz.co.searchwellington.model.taggingvotes.voters.TaggingVoter

class GeotaggingVote(val geocode: Geocode, val voter: TaggingVoter, val weight: Int) {

  def getGeocode: Geocode = geocode
  def getVoter: TaggingVoter = voter

  override def toString = s"GeotaggingVote($geocode, $voter, $weight)"
}





