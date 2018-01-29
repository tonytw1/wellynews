package nz.co.searchwellington.filters.attributesetters

import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.repositories.{HibernateResourceDAO, TagDAO}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class CombinerPageAttributeSetter @Autowired()(var tagDAO: TagDAO, var resourceDAO: HibernateResourceDAO) extends AttributeSetter {

  private val log = Logger.getLogger(classOf[CombinerPageAttributeSetter])
  private val combinerPattern = Pattern.compile("^/(.*)\\+(.*?)(/rss|/json)?$")

  override def setAttributes(request: HttpServletRequest): Boolean = {
    val matcher = combinerPattern.matcher(request.getPathInfo)
    if (matcher.matches) {
      val left = matcher.group(1)
      val right = matcher.group(2)
      log.debug("Path matches combiner pattern for '" + left + "', '" + right + "'")

      tagDAO.loadTagByName(right).map { rightHandTag =>
        resourceDAO.getPublisherByUrlWords(left).map { publisher =>
          log.debug("Right matches tag: " + rightHandTag.getName + " and left matches publisher: " + publisher.getName)
          request.setAttribute("publisher", publisher)
          request.setAttribute("tag", rightHandTag)
          true

        }.getOrElse {
          tagDAO.loadTagByName(left).map { leftHandTag =>
            log.debug("Setting tags '" + leftHandTag.getName + "', '" + rightHandTag.getName + "'")
            val tags = Seq(leftHandTag, rightHandTag)
            request.setAttribute("tags", tags)
            true

          }.getOrElse {
            false
          }
        }
      }.getOrElse {
        false
      }

    } else {
      false
    }
  }

}
