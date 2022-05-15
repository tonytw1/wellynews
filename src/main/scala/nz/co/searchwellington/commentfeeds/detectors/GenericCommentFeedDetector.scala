package nz.co.searchwellington.commentfeeds.detectors

import java.net.URL
import java.util.regex.Pattern

class GenericCommentFeedDetector(regex: String) extends CommentFeedDetector {

  private val pattern = Pattern.compile(regex)

  override def isValid(url: URL) = url != null && pattern.matcher(url.toExternalForm).matches

}