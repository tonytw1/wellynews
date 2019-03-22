package nz.co.searchwellington.model.taggingvotes.voters

import nz.co.searchwellington.model.taggingvotes.TaggingVote
import nz.co.searchwellington.model.{Tag, User}

class HandTagging(val tag: Tag, user: User) extends TaggingVote {
  override def voter: TaggingVoter = user
}
