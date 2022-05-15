package nz.co.searchwellington.commentfeeds.detectors

import org.springframework.stereotype.Component
import java.net.URL

@Component
class BlogspotCommentFeedDetector extends CommentFeedDetector {
  override def isValid(url: URL) = {
    url.getHost.endsWith("blogspot.com") && url.getPath.contains("/comments/")
  }
}