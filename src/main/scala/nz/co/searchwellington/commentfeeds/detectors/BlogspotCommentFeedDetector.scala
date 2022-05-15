package nz.co.searchwellington.commentfeeds.detectors

import org.springframework.stereotype.Component
import java.net.URL

@Component
class BlogspotCommentFeedDetector extends CommentFeedDetector {
  override def isValid(url: URL) = url != null && url.toExternalForm.contains("blogspot.com") && url.toExternalForm.contains("/comments/")
}