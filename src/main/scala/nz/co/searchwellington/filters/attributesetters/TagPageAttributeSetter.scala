package nz.co.searchwellington.filters.attributesetters

import java.util.regex.Pattern

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await

@Component class TagPageAttributeSetter @Autowired()(var tagDAO: TagDAO, mongoRepository: MongoRepository) extends AttributeSetter
with ReasonableWaits {

  private val log = Logger.getLogger(classOf[TagPageAttributeSetter])
  private val tagPagePathPattern = Pattern.compile("^/(.*?)(/(comment|geotagged|autotag))?(/(rss|json))?$")

  override def setAttributes(request: HttpServletRequest): Boolean = {
    log.debug("Looking for single tag path")
    val contentMatcher = tagPagePathPattern.matcher(request.getPathInfo)
    if (contentMatcher.matches) {
      val `match` = contentMatcher.group(1)
      if (!(isReservedUrlWord(`match`))) {
        log.debug("'" + `match` + "' matches content")
        log.debug("Looking for tag '" + `match` + "'")

        Await.result(mongoRepository.getTagByUrlWords(`match`), TenSeconds).map { tag =>
          log.debug("Setting tag: " + tag.getName)
          request.setAttribute("tag", tag) // TODO deprecate
          val tags = Seq(tag)
          log.debug("Setting tags: " + tags)
          request.setAttribute("tags", tags)
          true
        }.getOrElse{
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