package nz.co.searchwellington.filters.attributesetters

import java.util.regex.{Matcher, Pattern}
import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.repositories.HibernateResourceDAO
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class PublisherPageAttributeSetter @Autowired()(var resourceDAO: HibernateResourceDAO) extends AttributeSetter {

  private val log = Logger.getLogger(classOf[PublisherPageAttributeSetter])
  private val publisherPagePathPattern = Pattern.compile("^/(.*?)(/(comment|geotagged))?(/(edit|save|rss|json))?$")

  def setAttributes(request: HttpServletRequest): Boolean = {
    log.debug("Looking for single publisher path")
    val contentMatcher: Matcher = publisherPagePathPattern.matcher(request.getPathInfo)
    if (contentMatcher.matches) {
      val `match`: String = contentMatcher.group(1)
      log.debug("'" + `match` + "' matches content")
      log.debug("Looking for publisher '" + `match` + "'")
       resourceDAO.getPublisherByUrlWords(`match`).map { publisher =>
        log.info("Setting publisher: " + publisher.getName)
        request.setAttribute("publisher", publisher)
        request.setAttribute("resource", publisher)
         true
      }.getOrElse{
         false
       }
    } else {
      false
    }
  }

}
