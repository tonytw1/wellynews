package nz.co.searchwellington.commentfeeds.detectors

import java.net.URL

trait CommentFeedDetector {
  def isValid(url: URL): Boolean
}