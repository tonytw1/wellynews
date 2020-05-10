package nz.co.searchwellington.model.taggingvotes

import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.model.taggingvotes.voters.TaggingVoter

trait TaggingVote {
  def tag: Tag
  def voter: TaggingVoter

  def getTag: Tag = tag
  def getVoter: TaggingVoter = voter

}
