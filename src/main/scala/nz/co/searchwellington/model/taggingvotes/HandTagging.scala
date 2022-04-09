package nz.co.searchwellington.model.taggingvotes

import nz.co.searchwellington.model.{Tag, User}

case class HandTagging(tag: Tag, taggingUser: User, reason: Option[String] = None) extends TaggingVote {
  override def explanation: String = {
    "Hand tagged by " + taggingUser.getDisplayName +
      reason.map(r => " (" + r + ")").getOrElse("")
  }

  def user: Option[User] = Some(taggingUser)
}