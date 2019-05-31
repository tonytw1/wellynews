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

@Component class CombinerPageAttributeSetter @Autowired()(tagDAO: TagDAO, mongoRepository: MongoRepository)
  extends AttributeSetter with ReasonableWaits {

  private val log = Logger.getLogger(classOf[CombinerPageAttributeSetter])
  private val combinerPattern = Pattern.compile("^/(.*)\\+(.*?)(/rss|/json)?$")

  override def setAttributes(request: HttpServletRequest): Boolean = {
    val matcher = combinerPattern.matcher(request.getPathInfo)
    val x = if (matcher.matches) {
      val left = matcher.group(1)
      val right = matcher.group(2)
      log.info("Path matches combiner pattern for '" + left + "', '" + right + "'")

      Await.result(mongoRepository.getTagByUrlWords(right), TenSeconds).map { rightHandTag =>

        Await.result(mongoRepository.getWebsiteByUrlwords(left), TenSeconds).map { publisher =>
          log.info("Right matches tag: " + rightHandTag.getName + " and left matches publisher: " + publisher.title.getOrElse(publisher.id))
          request.setAttribute("publisher", publisher)
          request.setAttribute("tag", rightHandTag)
          true

        }.getOrElse {
          Await.result(mongoRepository.getTagByUrlWords(left), TenSeconds).map { leftHandTag =>
            log.info("Setting tags '" + leftHandTag.getName + "', '" + rightHandTag.getName + "'")
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

    log.info("Returning: " + x)
    x
  }

}
