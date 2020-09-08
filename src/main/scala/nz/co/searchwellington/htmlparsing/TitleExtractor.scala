package nz.co.searchwellington.htmlparsing

import org.apache.log4j.Logger
import org.htmlparser.Parser
import org.htmlparser.filters.TagNameFilter
import org.htmlparser.util.ParserException
import org.springframework.stereotype.Component

@Component
class TitleExtractor {

  private val log = Logger.getLogger(classOf[SnapshotBodyExtractor])

  def extractTitle(htmlPage: String): Option[String] = {
    log.info("Extracting title")
    try {
      val parser = new Parser
      parser.setInputHTML(htmlPage)

      val titleTagFilter = new TagNameFilter("TITLE")
      val list = parser.extractAllNodesThatMatch(titleTagFilter)
      log.info("Found matching nodes: " + list.size())
      if (list.size > 0) {
        val title = list.elementAt(0)
        log.info("Found title: " + title)
        Some(title.toPlainTextString)
      } else {
        log.info("No title found")
        None
      }

    } catch {
      case e: ParserException =>
        log.warn("Parser exception while extracting title", e)
        None
      case e: Exception =>
        log.error("Exception while extracting title", e)
        None
    }
  }

}
