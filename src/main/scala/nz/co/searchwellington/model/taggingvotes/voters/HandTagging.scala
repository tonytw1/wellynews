package nz.co.searchwellington.model.taggingvotes.voters

import nz.co.searchwellington.model.taggingvotes.TaggingVote
import nz.co.searchwellington.model.{Tag, User}

class HandTagging(val tag: Tag, taggingUser: User) extends TaggingVote {

  override def explanation: String = "Hand tagged"

  override def user: Option[User] = Some(taggingUser)

}
