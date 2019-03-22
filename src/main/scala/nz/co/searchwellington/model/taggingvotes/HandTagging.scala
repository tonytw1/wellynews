package nz.co.searchwellington.model.taggingvotes

import nz.co.searchwellington.model.taggingvotes.voters.TaggingVoter
import nz.co.searchwellington.model.{Tag, User}

class HandTagging(val tag: Tag, val user: User) extends TaggingVote {
  override def voter: TaggingVoter = user
}
