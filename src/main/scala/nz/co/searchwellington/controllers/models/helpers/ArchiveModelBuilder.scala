package nz.co.searchwellington.controllers.models.helpers

import java.text.{ParseException, SimpleDateFormat}
import java.util.{Date, List}
import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.model.ArchiveLink
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.dates.DateFormatter

@Component class ArchiveModelBuilder @Autowired() (contentRetrievalService: ContentRetrievalService, archiveLinksService: ArchiveLinksService, dateFormatter: DateFormatter) extends ModelBuilder {

  private var log: Logger = Logger.getLogger(classOf[ArchiveModelBuilder])

  def isValid(request: HttpServletRequest): Boolean = {
    return request.getPathInfo.matches("^/archive/.*?/.*?$")
  }

  def populateContentModel(request: HttpServletRequest): ModelAndView = {
    if (isValid(request)) {
      log.debug("Building archive page model")
      val month: Date = getArchiveDateFromPath(request.getPathInfo)
      if (month != null) {
        log.debug("Archive month is: " + month)
        val monthLabel: String = new DateFormatter().fullMonthYear(month)
        val mv: ModelAndView = new ModelAndView
        mv.addObject("heading", monthLabel)
        mv.addObject("description", "Archived newsitems for the month of " + dateFormatter.fullMonthYear(month))
        mv.addObject("main_content", contentRetrievalService.getNewsitemsForMonth(month))
        return mv
      }
    }
    return null
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    val month: Date = getArchiveDateFromPath(request.getPathInfo)
    val archiveLinks: List[ArchiveLink] = contentRetrievalService.getArchiveMonths
    populateNextAndPreviousLinks(mv, month, archiveLinks)
    archiveLinksService.populateArchiveLinks(mv, archiveLinks)
  }

  def getViewName(mv: ModelAndView): String = {
    return "archivePage"
  }

  private def populateNextAndPreviousLinks(mv: ModelAndView, month: Date, archiveLinks: List[ArchiveLink]) {
    var selected: ArchiveLink = null
    import scala.collection.JavaConversions._
    for (link <- archiveLinks) {
      if (link.getMonth == month) {
        selected = link
      }
    }
    if (selected != null) {
      val indexOf: Int = archiveLinks.indexOf(selected)
      if (indexOf < archiveLinks.size - 1) {
        val previous: ArchiveLink = archiveLinks.get(indexOf + 1)
        mv.addObject("next_page", previous)
      }
      if (indexOf > 0) {
        val next: ArchiveLink = archiveLinks.get(indexOf - 1)
        mv.addObject("previous_page", next)
      }
    }
  }

  private def getArchiveDateFromPath(path: String): Date = {
    if (path.startsWith("/archive/")) {
      val fields: Array[String] = path.split("/")
      if (fields.length == 4) {
        val archiveMonthString: String = fields(2) + " " + fields(3)
        val df: SimpleDateFormat = new SimpleDateFormat("yyyy MMM")
        try {
          val month: Date = df.parse(archiveMonthString)
          return month
        }
        catch {
          case e: ParseException => {
            throw (new IllegalArgumentException(e.getMessage))
          }
        }
      }
    }
    return null
  }

}