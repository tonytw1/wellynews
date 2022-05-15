package nz.co.searchwellington.commentfeeds.detectors

import nz.co.searchwellington.model.Resource
import org.springframework.stereotype.Component

import java.net.URL
import java.util.regex.Pattern

@Deprecated // This is an example of exisiting newsitem page feed
@Component
class EyeOfTheFishCommentFeedDetector extends CommentFeedDetector {

  private val commentFeedUrlPattern = {
    Pattern.compile("""^http://eyeofthefish.org/.*/feed/$""")
  }

  override def isValid(url: URL, source: Resource): Boolean = {
    commentFeedUrlPattern.matcher(url.toExternalForm).matches
  }
}