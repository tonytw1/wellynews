package nz.co.searchwellington.commentfeeds.detectors

import org.springframework.stereotype.Component
import java.net.URL

@Component
class CommentSlashFeedDetector extends CommentFeedDetector {
  override def isValid(url: URL) = new GenericCommentFeedDetector("^.*\\/comments/feed\\/$").isValid(url)
}