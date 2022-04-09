package nz.co.searchwellington.model.taggingvotes

import nz.co.searchwellington.model.{Tag, User}

case class GeneratedTaggingVote(tag: Tag, explanation: String) extends TaggingVote {
  def user: Option[User] = None
}
