package nz.co.searchwellington.filters.attributesetters

import java.util.regex.{Matcher, Pattern}

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Component class PublisherPageAttributeSetter @Autowired()(mongoRepository: MongoRepository)
  extends AttributeSetter with ReasonableWaits {

  private val log = Logger.getLogger(classOf[PublisherPageAttributeSetter])
  private val publisherPagePathPattern = Pattern.compile("^/(.*?)(/(comment|geotagged|.*?-.*?))?(/(edit|save|rss|json))?$")

  def setAttributes(request: HttpServletRequest): Boolean = {
    val contentMatcher: Matcher = publisherPagePathPattern.matcher(request.getPathInfo)
    if (contentMatcher.matches) {
      val `match` = contentMatcher.group(1)
      log.debug("'" + `match` + "' matches content")
      log.debug("Looking for publisher '" + `match` + "'")
      Await.result(mongoRepository.getWebsiteByUrlwords(`match`), TenSeconds).map { publisher =>
        request.setAttribute("publisher", publisher)
        request.setAttribute("resource", publisher)
        true
      }.getOrElse {
        false
      }
    } else {
      false
    }
  }

}
