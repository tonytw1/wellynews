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

@Component class PublisherPageAttributeSetter @Autowired()(mongoRepository: MongoRepository)
  extends AttributeSetter with ReasonableWaits {

  private val publisherPagePathPattern = Pattern.compile("^/(.*?)(/(geotagged|.*?-.*?))?(/(rss|json))?$")

  def setAttributes(request: HttpServletRequest): Future[Boolean] = {
    val contentMatcher = publisherPagePathPattern.matcher(RequestPath.getPathFrom(request))
    if (contentMatcher.matches) {
      val publisherUrlWords = contentMatcher.group(1)
      if (publisherUrlWords.trim.nonEmpty && !publisherUrlWords.contains("+")) {
        mongoRepository.getWebsiteByUrlwords(publisherUrlWords).map { maybeWebsite =>
          maybeWebsite.exists { publisher =>
            request.setAttribute("publisher", publisher)
            request.setAttribute("resource", publisher)
            true
          }
        }
      } else {
        Future.successful(false)
      }
    } else {
      Future.successful(false)
    }
  }

}
