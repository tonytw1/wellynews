package nz.co.searchwellington.filters.attributesetters

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component
class FeedAttributeSetter @Autowired()(mongoRepository: MongoRepository) extends AttributeSetter with ReasonableWaits {

  private val feedPattern = Pattern.compile("^/feed/(.*?)(/(edit|save|rss|json|accept-all))?$")

  override def setAttributes(request: HttpServletRequest): Future[Map[String, Any]] = {
    val contentMatcher = feedPattern.matcher(RequestPath.getPathFrom(request))
    if (contentMatcher.matches) {
      val urlWords = contentMatcher.group(1)
      mongoRepository.getFeedByUrlwords(urlWords).map { maybeFeed =>
          maybeFeed.map { feed =>
            Map (
              FeedAttributeSetter.FEED_ATTRIBUTE -> feed,
              "resource" -> feed
            )
          }.getOrElse{
            Map.empty
          }
      }
    } else {
      Future.successful(Map.empty)
    }
  }
}

object FeedAttributeSetter {
  val FEED_ATTRIBUTE = "feedAttribute"
}
