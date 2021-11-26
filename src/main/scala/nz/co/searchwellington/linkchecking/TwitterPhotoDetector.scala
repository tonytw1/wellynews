package nz.co.searchwellington.linkchecking
import nz.co.searchwellington.model.Resource
import org.apache.log4j.Logger
import org.htmlparser.filters.{AndFilter, HasAttributeFilter, NodeClassFilter, TagNameFilter}
import org.htmlparser.{Parser, Tag}
import org.joda.time.DateTime
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Component
class TwitterPhotoDetector extends LinkCheckerProcessor {

  private val log = Logger.getLogger(classOf[TwitterPhotoDetector])

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
