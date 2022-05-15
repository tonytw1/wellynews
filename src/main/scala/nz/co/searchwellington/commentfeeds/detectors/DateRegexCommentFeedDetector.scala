package nz.co.searchwellington.commentfeeds.detectors

import nz.co.searchwellington.model.Resource

import java.net.URL
import java.util.regex.Pattern

class DateRegexCommentFeedDetector extends CommentFeedDetector {

  private val yearMonthDateRegex = {
    Pattern.compile(""".*\d{4}/\d{2}/\d{2}.*""")
  }

  override def isValid(url: URL, source: Resource): Boolean = {
    yearMonthDateRegex.matcher(url.toExternalForm).matches
  }

}