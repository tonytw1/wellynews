package nz.co.searchwellington.htmlparsing

import org.apache.commons.lang.StringEscapeUtils
import org.apache.log4j.Logger
import org.htmlparser.filters.{AndFilter, HasAttributeFilter, NodeClassFilter, TagNameFilter}
import org.htmlparser.util.ParserException
import org.htmlparser.{Node, Parser, Tag}
import org.springframework.stereotype.Component

@Component class RssLinkExtractor extends LinkExtractor {

  private val log = Logger.getLogger(classOf[RssLinkExtractor])

  override def extractLinks(inputHTML: String): Seq[String] = {

    def getLinksFrom(html: String): Seq[Node] = {
      val parser = new Parser
      try {
        parser.setInputHTML(html)
        val filterNode = new AndFilter(new TagNameFilter("LINK"), new AndFilter(new HasAttributeFilter("rel"), new HasAttributeFilter("href")))
        val filter = new AndFilter(filterNode, new NodeClassFilter(classOf[Tag]))

        parser.extractAllNodesThatMatch(filter).toNodeArray.toSeq

      } catch {
        case e: ParserException =>
          log.error("ParserException will trying to extract links.")
          Seq.empty
      }
    }

    val linkTags = getLinksFrom(inputHTML).map(_.asInstanceOf[Tag])

    val feedLinks = linkTags.filter { tag =>
      Option(tag.getAttribute("type")).exists { `type` =>
        `type` == "application/rss+xml" || `type` == "application/atom+xml"
      }
    }

    feedLinks.map { feedLink =>
      StringEscapeUtils.unescapeHtml(feedLink.getAttribute("href"))
    }
  }
}