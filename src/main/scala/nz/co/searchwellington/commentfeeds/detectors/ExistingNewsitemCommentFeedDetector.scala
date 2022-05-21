package nz.co.searchwellington.commentfeeds.detectors

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Newsitem, Resource}
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.net.URL

@Component
class ExistingNewsitemCommentFeedDetector @Autowired()()
  extends CommentFeedDetector with ReasonableWaits {

  private val log = LogFactory.getLog(classOf[ExistingNewsitemCommentFeedDetector])

  private val feedSuffixes = Seq("/feed", "/feed/", "feed/")

  override def isValid(url: URL, source: Resource): Boolean = {
    // If a feed url matches the url of an existing newsitem with /feed appended
    // then it is probably that newsitem's comment feed
    source match {
      case n: Newsitem =>
        feedSuffixes.exists { suffix =>
          val urlString = url.toExternalForm
          if (urlString.endsWith(suffix)) {
            val newsitemUrl = urlString.dropRight(suffix.length)
            log.info("Checking for existing newsitem with url: " + newsitemUrl)
            if (newsitemUrl == source.page) {
              log.info(s"Feed url $url appears to be a comment feed for newsitem: " + n.page)
              true
            } else {
              false
            }
          } else {
            false
          }
        }

      case _ =>
        // This check is only applicable to newsitems
        false
    }
  }

}
