package nz.co.searchwellington.filters.attributesetters

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

// Handles [website | publisher + tag] combiners
// The right hand side must be a tag; the left could be a website or publisher
@Component class CombinerPageAttributeSetter @Autowired()(mongoRepository: MongoRepository)
  extends AttributeSetter with ReasonableWaits {

  private val log = LogFactory.getLog(classOf[CombinerPageAttributeSetter])
  private val combinerPattern = Pattern.compile("^/(.*)\\+(.*?)(/rss|/json)?$")

  override def setAttributes(request: HttpServletRequest): Future[Boolean] = {
    val matcher = combinerPattern.matcher(RequestPath.getPathFrom(request))
    val eventualAnswer = if (matcher.matches) {
      val left = matcher.group(1)
      val right = matcher.group(2)
      log.debug("Path matches combiner pattern for '" + left + "', '" + right + "'")

      val eventualMaybeRightHandTag = mongoRepository.getTagByUrlWords(right)

      for {
        maybeRightHandTag <- eventualMaybeRightHandTag
        result <- {
          maybeRightHandTag.map { rightHandTag =>
            val eventualMaybeLeftWebsite = mongoRepository.getWebsiteByUrlwords(left)
            val eventualMaybeLeftTag = mongoRepository.getTagByUrlWords(left)
            for {
              maybeLeftWebsite <- eventualMaybeLeftWebsite
              maybeLeftTag <- eventualMaybeLeftTag
            } yield {
              maybeLeftWebsite.map { publisher =>
                log.debug("Right matches tag: " + rightHandTag.getName + " and left matches publisher: " + publisher.getTitle)
                request.setAttribute("publisher", publisher)
                request.setAttribute("tag", rightHandTag)
                true
              }.getOrElse {
                maybeLeftTag.exists { leftHandTag =>
                  log.debug("Setting tags '" + leftHandTag.getName + "', '" + rightHandTag.getName + "'")
                  val tags = Seq(leftHandTag, rightHandTag)
                  request.setAttribute("tags", tags)
                  true
                }
              }
            }
          }.getOrElse {
            Future.successful(false)
          }
        }
      } yield {
        result
      }

    } else {
      Future.successful(false)
    }

    eventualAnswer
  }
}
