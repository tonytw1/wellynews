package nz.co.searchwellington.filters.attributesetters

import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.filters.attributesetters.PublisherPageAttributeSetter.PUBLISHER_ATTRIBUTE
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.regex.Pattern
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component
class PublisherPageAttributeSetter @Autowired()(mongoRepository: MongoRepository)
  extends AttributeSetter with ReasonableWaits {

  private val publisherPagePathPattern = Pattern.compile("^/(.*?)(/(geotagged|.*?-.*?))?(/(rss|json))?$")

  def setAttributes(request: HttpServletRequest): Future[Map[String, Any]] = {
    val contentMatcher = publisherPagePathPattern.matcher(RequestPath.getPathFrom(request))
    if (contentMatcher.matches) {
      val publisherUrlWords = contentMatcher.group(1)
      if (publisherUrlWords.trim.nonEmpty && !publisherUrlWords.contains("+")) {
        mongoRepository.getWebsiteByUrlwords(publisherUrlWords).map { maybeWebsite =>
          maybeWebsite.map { publisher =>
            Map(
              PUBLISHER_ATTRIBUTE -> publisher
            )
          }.getOrElse {
            Map.empty
          }
        }
      } else {
        Future.successful(Map.empty)
      }
    } else {
      Future.successful(Map.empty)
    }
  }

}

object PublisherPageAttributeSetter {
  val PUBLISHER_ATTRIBUTE = "publisher"
}

