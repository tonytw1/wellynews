package nz.co.searchwellington.commentfeeds.detectors

import nz.co.searchwellington.model.Resource
import org.springframework.stereotype.Component

import java.net.URL

@Component
class CommentSlashFeedDetector extends CommentFeedDetector {
  override def isValid(url: URL, source: Resource) = new GenericCommentFeedDetector("^.*\\/comments/feed\\/$").isValid(url, source)
}