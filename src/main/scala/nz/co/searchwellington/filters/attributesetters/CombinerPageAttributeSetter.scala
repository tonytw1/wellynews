package nz.co.searchwellington.filters.attributesetters

import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.regex.Pattern
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

// Handles [website | publisher + tag] combiners
// The right hand side must be a tag; the left could be a website or publisher
@Component class CombinerPageAttributeSetter @Autowired()(mongoRepository: MongoRepository)
  extends AttributeSetter with ReasonableWaits {

  private val log = LogFactory.getLog(classOf[CombinerPageAttributeSetter])
  private val combinerPattern = Pattern.compile("^/(.*)\\+(.*?)(/rss|/json)?$")

  override def setAttributes(request: HttpServletRequest): Future[Map[String, Any]] = {
    val matcher = combinerPattern.matcher(RequestPath.getPathFrom(request))

    val eventualAnswer: Future[Map[String, Any]] = if (matcher.matches) {
      val left = matcher.group(1)
      val right = matcher.group(2)
      log.debug("Path matches combiner pattern for '" + left + "', '" + right + "'")

      val eventualMaybeRightHandTag = mongoRepository.getTagByUrlWords(right)

      for {
        maybeRightHandTag <- eventualMaybeRightHandTag
        result <- {
          val z: Future[Map[String, Any]] = maybeRightHandTag.map { rightHandTag =>
            val eventualMaybeLeftWebsite = mongoRepository.getWebsiteByUrlwords(left)
            val eventualMaybeLeftTag = mongoRepository.getTagByUrlWords(left)
            for {
              maybeLeftWebsite <- eventualMaybeLeftWebsite
              maybeLeftTag <- eventualMaybeLeftTag
            } yield {
              maybeLeftWebsite.map { publisher =>
                log.debug("Right matches tag: " + rightHandTag.getName + " and left matches publisher: " + publisher.getTitle)
                Map(
                  "publisher" -> publisher,
                  "tag" -> rightHandTag
                )
              }.getOrElse {
                maybeLeftTag.map { leftHandTag =>
                  log.debug("Setting tags '" + leftHandTag.getName + "', '" + rightHandTag.getName + "'")
                  val tags = Seq(leftHandTag, rightHandTag)
                  request.setAttribute("tags", tags)
                  Map(
                    "tags" -> tags
                  )
                }.getOrElse {
                  Map.empty
                }
              }
            }

          }.getOrElse {
            Future.successful(Map.empty)
          }
          z
        }

      } yield {
        result
      }

    } else {
      Future.successful(Map.empty)
    }

    eventualAnswer
  }
}
