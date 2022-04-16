package nz.co.searchwellington.commentfeeds.detectors

import java.util.regex.Pattern

class EyeOfTheFishCommentFeedDetector extends CommentFeedDetector {

  private val commentFeedUrlPattern = {
    Pattern.compile("""^http://eyeofthefish.org/.*/feed/$""")
  }

  override def isValid(url: String) = {
    commentFeedUrlPattern.matcher(url).matches
  }
}