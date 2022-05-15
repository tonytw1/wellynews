package nz.co.searchwellington.commentfeeds.detectors

import nz.co.searchwellington.model.Resource

import java.net.URL
import java.util.regex.Pattern

class GenericCommentFeedDetector(regex: String) extends CommentFeedDetector {

  private val pattern = Pattern.compile(regex)

  override def isValid(url: URL, source: Resource) = pattern.matcher(url.toExternalForm).matches

}