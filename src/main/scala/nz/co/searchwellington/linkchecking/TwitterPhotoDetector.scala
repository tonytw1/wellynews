package nz.co.searchwellington.linkchecking
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Newsitem, Resource}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.htmlparser.filters.{AndFilter, HasAttributeFilter, NodeClassFilter, TagNameFilter}
import org.htmlparser.{Parser, Tag}
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Component
class TwitterPhotoDetector @Autowired()(mongoRepository: MongoRepository)
  extends LinkCheckerProcessor with ReasonableWaits {

  private val log = LogFactory.getLog(classOf[TwitterPhotoDetector])

  private val metaTags = new AndFilter(new TagNameFilter("META"), new NodeClassFilter(classOf[Tag]))
  private val twitterPhotoMetaTags = new AndFilter(metaTags, new HasAttributeFilter("name", "twitter:image"))

  override def process(checkResource: Resource, maybePageContent: Option[String], seen: DateTime)(implicit ec: ExecutionContext): Future[Boolean] = {
    maybePageContent.map { pageContent =>
      parserFor(pageContent).flatMap { parser =>
        Try {
          val tags = parser.extractAllNodesThatMatch(twitterPhotoMetaTags).toNodeArray.toSeq.map(_.asInstanceOf[Tag])
          val imageURLs = tags.flatMap(tag => Option(tag.getAttribute("content")))
          if (imageURLs.nonEmpty) {
            log.info("Found twitter:images: " + imageURLs.mkString(", "))
            checkResource match {
              case newsitem: Newsitem =>
                log.info("Set twitter:images: " + imageURLs.mkString(", "))
                newsitem.twitterImage = imageURLs.headOption.map(_.trim)
                Await.result(mongoRepository.saveResource(newsitem), TenSeconds)
            }
          }
        }
      } match {
        case Success(_) =>
          Future.successful(true)
        case Failure(e) =>
          log.warn("Failed to parse html for twitter:images", e)
          Future.successful(false)
      }

    }.getOrElse{
      Future.successful(true)
    }
  }

  private def parserFor(html: String): Try[Parser] = {
    Try {
      val parser = new Parser
      parser.setInputHTML(html)
      parser
    }
  }

}
