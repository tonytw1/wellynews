package nz.co.searchwellington.filters.attributesetters

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Component class PublisherPageAttributeSetter @Autowired()(mongoRepository: MongoRepository)
  extends AttributeSetter with ReasonableWaits {

  private val log = LogFactory.getLog(classOf[PublisherPageAttributeSetter])
  private val publisherPagePathPattern = Pattern.compile("^/(.*?)(/(geotagged|.*?-.*?))?(/(rss|json))?$")

  def setAttributes(request: HttpServletRequest): Boolean = {
    val contentMatcher = publisherPagePathPattern.matcher(RequestPath.getPathFrom(request))
    if (contentMatcher.matches) {
      val publisherUrlWords = contentMatcher.group(1)
      if (publisherUrlWords.trim.nonEmpty) {
        log.debug("'" + publisherUrlWords + "' matches content")
        log.debug("Looking for publisher '" + publisherUrlWords + "'")
        val maybeWebsite = Await.result(mongoRepository.getWebsiteByUrlwords(publisherUrlWords), TenSeconds)

        maybeWebsite.exists { publisher =>
          request.setAttribute("publisher", publisher)
          request.setAttribute("resource", publisher)
          true
        }
      } else {
        false
      }

    } else {
      false
    }
  }

}
