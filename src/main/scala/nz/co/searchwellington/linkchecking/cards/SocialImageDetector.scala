package nz.co.searchwellington.linkchecking.cards

import org.htmlparser.{Parser, Tag}
import org.apache.commons.logging.LogFactory
import org.htmlparser.filters.{AndFilter, HasAttributeFilter, NodeClassFilter, OrFilter, TagNameFilter}
import org.springframework.stereotype.Component

import scala.util.{Failure, Success, Try}

@Component
class SocialImageDetector {

  private val log = LogFactory.getLog(classOf[SocialImageDetector])

  private val ogImage = new OrFilter(
    new HasAttributeFilter("name", "og:image"),
    new HasAttributeFilter("property", "og:image")
  )
  private val twitterImage = new HasAttributeFilter("name", "twitter:image")
  private val imageMetaTags = new OrFilter(ogImage, twitterImage)

  private val metaTags = new AndFilter(new TagNameFilter("META"), new NodeClassFilter(classOf[Tag]))

  private val metaImageTags = new AndFilter(metaTags, imageMetaTags)

  def extractSocialImageUrlsFrom(pageContent: String): Option[Seq[String]] = {
    parserFor(pageContent).flatMap { parser =>
      Try {
        val tags = parser.extractAllNodesThatMatch(metaImageTags).toNodeArray.toSeq.map(_.asInstanceOf[Tag])

        val imageURLs = tags.flatMap(tag => Option(tag.getAttribute("content")))
        imageURLs
      }

    } match {
      case Success(detected) =>
        Some(detected)
      case Failure(e) =>
        log.warn("Failed to detect social images", e)
        None
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
