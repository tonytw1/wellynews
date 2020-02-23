package nz.co.searchwellington.controllers.models.helpers

import java.text.{ParseException, SimpleDateFormat}
import java.util.Date

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.model.ArchiveLink
import nz.co.searchwellington.model.helpers.ArchiveLinksService
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.apache.log4j.Logger
import org.joda.time.{DateTime, DateTimeZone, Interval}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.dates.DateFormatter

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Component class ArchiveModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService, archiveLinksService: ArchiveLinksService,
                                                  loggedInUserFilter: LoggedInUserFilter) extends
  ModelBuilder with ReasonableWaits {

  private val log = Logger.getLogger(classOf[ArchiveModelBuilder])

  private val dateFormatter = new DateFormatter(DateTimeZone.UTC)

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/archive/.*?/.*?$")
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {
    if (isValid(request)) {
      getArchiveDateFromPath(request.getPathInfo).map { month =>
        val monthLabel = dateFormatter.fullMonthYear(month)

        val mv = new ModelAndView
        mv.addObject("heading", monthLabel)
        mv.addObject("description", "Archived newsitems for the month of " + dateFormatter.fullMonthYear(month))
        import scala.collection.JavaConverters._
        mv.addObject(MAIN_CONTENT, Await.result(contentRetrievalService.getNewsitemsForInterval(intervalForMonth(month), Option(loggedInUserFilter.getLoggedInUser)), TenSeconds).asJava)
      }

    } else {
      None
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    getArchiveDateFromPath(request.getPathInfo).map { month =>
      val eventualArchiveMonths = contentRetrievalService.getArchiveMonths(Option(loggedInUserFilter.getLoggedInUser))
      val eventualArchiveCounts = contentRetrievalService.getArchiveCounts(Option(loggedInUserFilter.getLoggedInUser))
      val eventualMonthPublishers = contentRetrievalService.getPublisherForInterval(intervalForMonth(month), Option(loggedInUserFilter.getLoggedInUser))

      Await.result(for {
        archiveLinks <- eventualArchiveMonths
        archiveStatistics <- eventualArchiveCounts
        monthPublishers <- eventualMonthPublishers
      } yield {
        populateNextAndPreviousLinks(mv, month, archiveLinks)
        archiveLinksService.populateArchiveLinks(mv, archiveLinks, archiveStatistics)
        import scala.collection.JavaConverters._
        mv.addObject("publishers", monthPublishers.map(_._1).asJava)

      }, TenSeconds)
    }
  }

  def getViewName(mv: ModelAndView): String = "archivePage"

  private def populateNextAndPreviousLinks(mv: ModelAndView, month: Date, archiveLinks: Seq[ArchiveLink]) {
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

  private def getArchiveDateFromPath(path: String): Option[Date] = {
    if (path.startsWith("/archive/")) {
      val fields = path.split("/")
      if (fields.length == 4) {
        val archiveMonthString = fields(2) + " " + fields(3)
        val df: SimpleDateFormat = new SimpleDateFormat("yyyy MMM")
        try {
          return Some(df.parse(archiveMonthString))
        }
        catch {
          case e: ParseException => {
            throw (new IllegalArgumentException(e.getMessage))
          }
        }
      } else {
        None
      }
    } else {
      None
    }
  }

  private def intervalForMonth(month: Date): Interval = { // TODO merge with above
    new Interval(new DateTime(month), new DateTime(month).plusMonths(1))
  }

}
