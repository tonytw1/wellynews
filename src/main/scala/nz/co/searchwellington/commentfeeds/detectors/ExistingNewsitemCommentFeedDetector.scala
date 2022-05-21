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
        val possibleCommentFeedUrls = feedSuffixes.map{ suffix =>
          source.page + suffix
        }
        val maybeMatchingCommentFeed = possibleCommentFeedUrls.find { possibleCommentFeedUrl =>
          possibleCommentFeedUrl == url.toExternalForm
        }
        maybeMatchingCommentFeed.foreach { commentFeedUrl =>
          log.info(s"Feed url $commentFeedUrl appears to be a comment feed for newsitem: " + n.page)
        }
        maybeMatchingCommentFeed.nonEmpty

      case _ =>
        // This check is only applicable to newsitems
        false
    }
  }

}
