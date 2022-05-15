package nz.co.searchwellington.commentfeeds.detectors

import nz.co.searchwellington.model.Resource

import java.net.URL

trait CommentFeedDetector {
  def isValid(url: URL, source: Resource): Boolean
}