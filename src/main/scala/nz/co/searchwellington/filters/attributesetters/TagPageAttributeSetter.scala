package nz.co.searchwellington.filters.attributesetters

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component
class TagPageAttributeSetter @Autowired()(var tagDAO: TagDAO, mongoRepository: MongoRepository) extends AttributeSetter
  with ReasonableWaits {

  private val log = LogFactory.getLog(classOf[TagPageAttributeSetter])
  private val tagPagePathPattern = Pattern.compile("^/(.*?)(/(comment|geotagged|autotag|.*?-.*?))?(/(rss|json))?$")

  override def setAttributes(request: HttpServletRequest): Future[Map[String, Any]] = {
    log.debug("Looking for single tag path")
    val contentMatcher = tagPagePathPattern.matcher(RequestPath.getPathFrom(request))
    if (contentMatcher.matches) {
      val tagUrlWords = contentMatcher.group(1)
      log.debug("'" + tagUrlWords + "' matches content")

      if (tagUrlWords.trim.nonEmpty && !tagUrlWords.contains("+")) {
        log.debug("Looking for tag '" + tagUrlWords + "'")
        mongoRepository.getTagByUrlWords(tagUrlWords).map { maybeTag =>
          maybeTag.map { tag =>
            Map(
              "tag" -> tag, // TODO deprecate
              "tags" -> Seq(tag)
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

object TagPageAttributeSetter {
  val TAG = "tag"
}