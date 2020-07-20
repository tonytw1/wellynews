package nz.co.searchwellington.htmlparsing

import nz.co.searchwellington.model.Resource
import org.apache.log4j.Logger
import org.htmlparser.Parser
import org.htmlparser.filters.TagNameFilter
import org.htmlparser.util.ParserException
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.archiving.FilesystemSnapshotArchive

@Component class SnapshotBodyExtractor() {

  private val log = Logger.getLogger(classOf[SnapshotBodyExtractor])

  private val snapshotArchive = new FilesystemSnapshotArchive("/home/tony/snapshots")

  def extractLatestSnapshotBodyTextFor(resource: Resource): Option[String] = {
    val latestFor = snapshotArchive.getLatestFor(resource.page)
    val content = if (latestFor != null) latestFor.getBody
    else null
    if (content != null) return extractBodyText(content)
    null
  }

  def extractBodyText(htmlPage: String): Option[String] = {
    try {
      val parser = new Parser
      parser.setInputHTML(htmlPage)

      val bodyTagFilter = new TagNameFilter("BODY")
      val list = parser.extractAllNodesThatMatch(bodyTagFilter)
      if (list.size > 0) {
        val body = list.elementAt(0)
        Some(body.toPlainTextString)
      } else {
        None
      }

    } catch {
      case e: ParserException =>
        log.warn("Parser exception while extracting body", e)
        None
      case e: Exception =>
        log.error("Exception while extracting body", e)
        None
    }
  }

}
