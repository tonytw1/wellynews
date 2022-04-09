package nz.co.searchwellington.model.taggingvotes

import nz.co.searchwellington.model.{Tag, User}

trait TaggingVote {
  def tag: Tag
  def explanation: String
  def user: Option[User]

  def getTag: Tag = tag
  def getExplanation: String = explanation
  def getUser: User = user.orNull

}
