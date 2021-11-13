package nz.co.searchwellington.commentfeeds.detectors

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Newsitem
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Component
class ExistingNewsitemCommentFeedDetector @Autowired()(mongoRepository: MongoRepository)
  extends CommentFeedDetector with ReasonableWaits {

  private val log = Logger.getLogger(classOf[ExistingNewsitemCommentFeedDetector])

  private val feedSuffixes = Seq("/feed", "/feed/")

  override def isValid(url: String): Boolean = {
    // If a feed url matches the url of an existing newsitem with /feed appended
    // then it is probably that newsitem's comment feed
    feedSuffixes.exists { suffix =>
      if (url.endsWith(suffix)) {
        val newsitemUrl = url.dropRight(suffix.length)
        Await.result(mongoRepository.getResourceByUrl(newsitemUrl), TenSeconds) match {
          case n: Some[Newsitem] =>
            log.info(s"Feed url $url appears to be a comment feed for newsitem: " + n)
            true
          case None => false
          case _ => false
        }
      } else {
        false
      }
    }
  }
}