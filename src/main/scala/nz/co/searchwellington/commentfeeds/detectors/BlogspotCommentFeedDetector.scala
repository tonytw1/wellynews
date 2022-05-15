package nz.co.searchwellington.commentfeeds.detectors

import nz.co.searchwellington.model.Resource
import org.springframework.stereotype.Component

import java.net.URL

@Component
class BlogspotCommentFeedDetector extends CommentFeedDetector {
  override def isValid(url: URL, source: Resource): Boolean = {
    url.getHost.endsWith("blogspot.com") && url.getPath.contains("/comments/")
  }
}