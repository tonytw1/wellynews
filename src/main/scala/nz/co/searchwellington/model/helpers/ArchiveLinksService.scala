package nz.co.searchwellington.model.helpers

import java.util
import nz.co.searchwellington.model.ArchiveLink
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

@Component
class ArchiveLinksService @Autowired()(contentRetrievalService: ContentRetrievalService) {

  private val MaxBackIssues = 6

  def populateArchiveLinks(mv: ModelAndView, archiveMonths: Seq[ArchiveLink]): Unit = {

    def populateArchiveStatistics(mv: ModelAndView): Unit = {
      val archiveStatistics = contentRetrievalService.getArchiveStatistics
      mv.addObject("site_count", archiveStatistics.get("W"))
      mv.addObject("newsitem_count", archiveStatistics.get("N"))
      mv.addObject("feed_count", archiveStatistics.get("F"))
    }

    mv.addObject("archive_links", archiveMonths.take(MaxBackIssues))

    populateArchiveStatistics(mv)
  }

}
