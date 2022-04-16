package nz.co.searchwellington.commentfeeds.detectors

import java.util.regex.Pattern

class WellingtonScoopCommentFeedDetector extends CommentFeedDetector {

  private val commentFeedUrlPattern = {
    Pattern.compile("""^http://wellington.scoop.co.nz/\?feed=rss2\&p=(\d+)$""")
  }
  private val httpsCommentFeedUrlPattern = {
    Pattern.compile("""^https://wellington.scoop.co.nz/\?feed=rss2\&p=(\d+)$""")
  }

  override def isValid(url: String): Boolean = {
    commentFeedUrlPattern.matcher(url).matches || httpsCommentFeedUrlPattern.matcher(url).matches
  }
}