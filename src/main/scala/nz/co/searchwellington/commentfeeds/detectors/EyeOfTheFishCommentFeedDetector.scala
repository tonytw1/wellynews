package nz.co.searchwellington.commentfeeds.detectors

import java.net.URL
import java.util.regex.Pattern

class EyeOfTheFishCommentFeedDetector extends CommentFeedDetector {

  private val commentFeedUrlPattern = {
    Pattern.compile("""^http://eyeofthefish.org/.*/feed/$""")
  }

  override def isValid(url: URL): Boolean = {
    commentFeedUrlPattern.matcher(url.toExternalForm).matches
  }
}