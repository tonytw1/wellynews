package nz.co.searchwellington.commentfeeds.detectors

import org.springframework.stereotype.Component

import java.net.URL
import java.util.regex.Pattern

@Component
class WellingtonScoopCommentFeedDetector extends CommentFeedDetector {

  private val commentFeedUrlPattern = {
    Pattern.compile("""^http://wellington.scoop.co.nz/\?feed=rss2\&p=(\d+)$""")
  }
  private val httpsCommentFeedUrlPattern = {
    Pattern.compile("""^https://wellington.scoop.co.nz/\?feed=rss2\&p=(\d+)$""")
  }

  override def isValid(url: URL): Boolean = {
    commentFeedUrlPattern.matcher(url.toString).matches || httpsCommentFeedUrlPattern.matcher(url.toExternalForm).matches
  }
}