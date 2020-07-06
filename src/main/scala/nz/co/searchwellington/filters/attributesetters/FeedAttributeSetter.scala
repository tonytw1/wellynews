package nz.co.searchwellington.filters.attributesetters

import java.util.regex.Pattern

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Component class FeedAttributeSetter @Autowired()(mongoRepository: MongoRepository) extends AttributeSetter with ReasonableWaits {

  private val log = Logger.getLogger(classOf[FeedAttributeSetter])

  private val FEED_ATTRIBUTE = "feedAttribute" // TODO private so not actually used?
  private val feedPattern = Pattern.compile("^/feed/(.*?)(/(edit|save|rss|json))?$")

  override def setAttributes(request: HttpServletRequest): Boolean = {
    val contentMatcher = feedPattern.matcher(RequestPath.getPathFrom(request))
    if (contentMatcher.matches) {
      val urlWords = contentMatcher.group(1)
      Await.result(mongoRepository.getFeedByUrlwords(urlWords), TenSeconds).map { feed =>
        request.setAttribute(FEED_ATTRIBUTE, feed)
        request.setAttribute("resource", feed)
        true
      }.getOrElse {
        false
      }

    } else {
      false
    }
  }

}
