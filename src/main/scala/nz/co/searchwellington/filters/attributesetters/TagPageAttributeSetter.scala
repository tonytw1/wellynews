package nz.co.searchwellington.filters.attributesetters

import java.util.regex.Pattern

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Component class TagPageAttributeSetter @Autowired()(var tagDAO: TagDAO, mongoRepository: MongoRepository) extends AttributeSetter
  with ReasonableWaits {

  private val log = LogFactory.getLog(classOf[TagPageAttributeSetter])
  private val tagPagePathPattern = Pattern.compile("^/(.*?)(/(comment|geotagged|autotag|.*?-.*?))?(/(rss|json))?$")

  override def setAttributes(request: HttpServletRequest): Boolean = {
    log.debug("Looking for single tag path")
    val contentMatcher = tagPagePathPattern.matcher(RequestPath.getPathFrom(request))
    if (contentMatcher.matches) {
      val tagUrlWords = contentMatcher.group(1)
      if (!isReservedUrlWord(tagUrlWords)) {
        log.debug("'" + tagUrlWords + "' matches content")

        if (tagUrlWords.trim.nonEmpty) {
          log.debug("Looking for tag '" + tagUrlWords + "'")
          Await.result(mongoRepository.getTagByUrlWords(tagUrlWords), TenSeconds).exists { tag =>
            log.debug("Setting tag: " + tag.getName)
            request.setAttribute(TagPageAttributeSetter.TAG, tag) // TODO deprecate
            val tags = Seq(tag)
            log.debug("Setting tags: " + tags)
            request.setAttribute("tags", tags)
            true
          }
        } else {
          false
        }
      } else {
        false
      }
    } else {
      false
    }
  }

  // TODO this wants to be in the spring config
  // TODO Push up
  private def isReservedUrlWord(urlWord: String): Boolean = {
    val reservedUrlWords = Seq("about",
      "api",
      "autotag",
      "index",
      "feeds",
      "comment",
      "geotagged",
      "tags")
    reservedUrlWords.contains(urlWord)
  }

}

object TagPageAttributeSetter {
  val TAG = "tag"
}