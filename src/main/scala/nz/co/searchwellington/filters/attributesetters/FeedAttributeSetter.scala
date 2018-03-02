package nz.co.searchwellington.filters.attributesetters

import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.repositories.HibernateResourceDAO
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class FeedAttributeSetter @Autowired()(resourceDAO: HibernateResourceDAO) extends AttributeSetter {

  private val log = Logger.getLogger(classOf[FeedAttributeSetter])
  private val FEED_ATTRIBUTE = "feedAttribute" // TODO private so not actually used?
  private val feedPattern = Pattern.compile("^/feed/(.*?)(/(edit|save|rss|json))?$")

  override def setAttributes(request: HttpServletRequest): Boolean = {
    val contentMatcher = feedPattern.matcher(request.getPathInfo)
    if (contentMatcher.matches) {
      val `match` = contentMatcher.group(1)
      log.debug("'" + `match` + "' matches content")
      log.debug("Looking for feed '" + `match` + "'")
      val feed: Feed = resourceDAO.loadFeedByUrlWords(`match`).asInstanceOf[Feed]
      if (feed != null) {
        log.debug("Setting feed: " + feed.title)
        request.setAttribute(FEED_ATTRIBUTE, feed)
        request.setAttribute("resource", feed)
        true
      } else {
        false
      }
    } else {
      false
    }
  }

}
