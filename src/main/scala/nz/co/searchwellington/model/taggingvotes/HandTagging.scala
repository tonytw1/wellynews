package nz.co.searchwellington.model.taggingvotes

import nz.co.searchwellington.model.{Tag, User}

case class HandTagging(tag: Tag, user: User, reason: Option[String] = None) extends TaggingVote {
  override def explanation: String = {
    "Hand tagged by " + user.getDisplayName +
      reason.map(r => " (" + r + ")").getOrElse("")
  }
}