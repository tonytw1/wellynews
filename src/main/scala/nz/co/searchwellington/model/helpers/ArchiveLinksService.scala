package nz.co.searchwellington.model.helpers

import nz.co.searchwellington.model.ArchiveLink
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

@Component
class ArchiveLinksService {

  private val MaxBackIssues = 6

  def populateArchiveLinks(mv: ModelAndView, archiveMonths: Seq[ArchiveLink], archiveStatistics: Map[String, Long]): Unit = {
    def populateArchiveStatistics(mv: ModelAndView): Unit = {
      mv.addObject("site_count", archiveStatistics.get("W").orNull)
      mv.addObject("newsitem_count", archiveStatistics.get("N").orNull)
      mv.addObject("feed_count", archiveStatistics.get("F").orNull)
    }

    import scala.collection.JavaConverters._
    mv.addObject("archive_links", archiveMonths.take(MaxBackIssues).asJava)

    populateArchiveStatistics(mv)
  }

}
