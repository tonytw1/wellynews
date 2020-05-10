package nz.co.searchwellington.model.taggingvotes

import nz.co.searchwellington.model.Tag

trait TaggingVote {
  def tag: Tag
  def explanation: String

  def getTag: Tag = tag
  def getExplanation: String = explanation

}
