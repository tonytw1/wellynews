package nz.co.searchwellington.model.taggingvotes

import nz.co.searchwellington.model.taggingvotes.voters.TaggingVoter
import nz.co.searchwellington.model.{Tag, User}

case class HandTagging(tag: Tag, user: User) extends TaggingVote {
  override def explanation: String = "Hand tagged by " + user.getDisplayName
}