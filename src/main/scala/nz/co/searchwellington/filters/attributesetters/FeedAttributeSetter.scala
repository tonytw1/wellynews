package nz.co.searchwellington.filters.attributesetters

import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest

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
      val urlWords = contentMatcher.group(1)
      resourceDAO.loadFeedByUrlWords(urlWords).map { feed =>
        log.debug("Setting feed: " + feed.title)
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
