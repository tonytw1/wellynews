package nz.co.searchwellington.commentfeeds.detectors

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Newsitem, Resource}
import org.apache.commons.logging.LogFactory
import org.springframework.stereotype.Component

import java.net.URL

@Component
class ExistingNewsitemCommentFeedDetector extends CommentFeedDetector with ReasonableWaits {

  private val log = LogFactory.getLog(classOf[ExistingNewsitemCommentFeedDetector])

  private val feedSuffixes = Seq("/feed", "/feed/", "feed/")

  override def isValid(url: URL, source: Resource): Boolean = {
    // If a feed url matches the url of an existing newsitem with /feed appended
    // then it is probably that newsitem's comment feed
    source match {
      case n: Newsitem =>
        val possibleCommentFeedUrls = feedSuffixes.map{ suffix =>
          new URL(source.page + suffix)
        }
        val maybeMatchingCommentFeed = possibleCommentFeedUrls.find { possibleCommentFeedUrl =>
          withoutProtocol(possibleCommentFeedUrl) == withoutProtocol(url)
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

  private def withoutProtocol(url: URL): String = {
    Option(url.getProtocol).map { p =>
      url.toExternalForm.replaceFirst(p + "://", "")
    }.getOrElse{
      url.toExternalForm
    }
  }

}
