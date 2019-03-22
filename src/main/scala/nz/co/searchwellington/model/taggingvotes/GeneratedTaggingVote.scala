package nz.co.searchwellington.model.taggingvotes
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.model.taggingvotes.voters.TaggingVoter

case class GeneratedTaggingVote(val tag: Tag, val voter: TaggingVoter) extends TaggingVote
