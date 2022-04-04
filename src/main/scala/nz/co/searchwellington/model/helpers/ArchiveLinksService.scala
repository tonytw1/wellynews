package nz.co.searchwellington.model.helpers

import nz.co.searchwellington.model.ArchiveLink
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap

import scala.jdk.CollectionConverters._

@Component
class ArchiveLinksService {

  private val MaxBackIssues = 6

  def populateArchiveLinks(archiveMonths: Seq[ArchiveLink], archiveStatistics: Map[String, Long]): ModelMap = {
    new ModelMap().addAttribute("archive_links", archiveMonths.take(MaxBackIssues).asJava)
      .addAttribute("site_count", archiveStatistics.get("W").orNull)
      .addAttribute("newsitem_count", archiveStatistics.get("N").orNull)
      .addAttribute("feed_count", archiveStatistics.get("F").orNull)
  }

}
