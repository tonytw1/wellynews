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

  def populateArchiveLinks(mv: ModelAndView, archiveMonths: util.List[ArchiveLink]): Unit = {

    def populateArchiveStatistics(mv: ModelAndView): Unit = {
      val archiveStatistics: util.Map[String, Integer] = contentRetrievalService.getArchiveStatistics
      if (archiveStatistics != null) {
        mv.addObject("site_count", archiveStatistics.get("W"))
        mv.addObject("newsitem_count", archiveStatistics.get("N"))
        mv.addObject("feed_count", archiveStatistics.get("F"))
      }
    }

    if (archiveMonths.size <= MaxBackIssues) {
      mv.addObject("archive_links", archiveMonths)
    }
    else {
      mv.addObject("archive_links", archiveMonths.subList(0, MaxBackIssues))
    }

    populateArchiveStatistics(mv)
  }

}
