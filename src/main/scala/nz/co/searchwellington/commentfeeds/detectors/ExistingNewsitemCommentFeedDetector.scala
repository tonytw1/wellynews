package nz.co.searchwellington.commentfeeds.detectors

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Newsitem, Resource}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Component
class ExistingNewsitemCommentFeedDetector @Autowired()(mongoRepository: MongoRepository)
  extends CommentFeedDetector with ReasonableWaits {

  private val log = LogFactory.getLog(classOf[ExistingNewsitemCommentFeedDetector])

  private val feedSuffixes = Seq("/feed", "/feed/", "feed/")

  override def isValid(url: String): Boolean = {
    // If a feed url matches the url of an existing newsitem with /feed appended
    // then it is probably that newsitem's comment feed
    feedSuffixes.exists { suffix =>
      if (url.endsWith(suffix)) {
        val newsitemUrl = url.dropRight(suffix.length)
        log.info("Checking for existing newsitem with url: " + newsitemUrl)
        val maybeResource = Await.result(mongoRepository.getResourceByUrl(newsitemUrl), TenSeconds)
        maybeResource.exists { resource =>
          resource match {
            case n: Newsitem =>
              log.info(s"Feed url $url appears to be a comment feed for newsitem: " + n)
              true
            case _ => false
          }
        }
      } else {
        false
      }
    }
  }
}
