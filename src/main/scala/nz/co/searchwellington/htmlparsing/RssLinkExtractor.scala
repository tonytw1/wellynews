package nz.co.searchwellington.htmlparsing

import org.apache.commons.lang.StringEscapeUtils
import org.apache.log4j.Logger
import org.htmlparser.filters.{AndFilter, HasAttributeFilter, NodeClassFilter, TagNameFilter}
import org.htmlparser.{Parser, Tag}
import org.springframework.stereotype.Component

import scala.util.{Failure, Success, Try}

@Component class RssLinkExtractor {

  private val log = Logger.getLogger(classOf[RssLinkExtractor])

  private val filterNode = new AndFilter(new TagNameFilter("LINK"), new AndFilter(new HasAttributeFilter("rel"), new HasAttributeFilter("href")))
  private val filter = new AndFilter(filterNode, new NodeClassFilter(classOf[Tag]))

  def extractFeedLinks(html: String): Seq[String] = {
    parserFor(html).flatMap { parser =>
      Try {
        val linkTags = parser.extractAllNodesThatMatch(filter).toNodeArray.toSeq.map(_.asInstanceOf[Tag])

        val feedLinks = linkTags.filter { tag =>
          Option(tag.getAttribute("type")).exists { `type` =>
            `type` == "application/rss+xml" || `type` == "application/atom+xml"
          }
        }

        feedLinks.map { feedLink =>
          StringEscapeUtils.unescapeHtml(feedLink.getAttribute("href"))
        }
      }

    } match {
      case Success(links) =>
        links
      case Failure(e) =>
        log.warn("Failed to parse html for links", e)
        Seq.empty
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