package nz.co.searchwellington.controllers.models.helpers

import java.text.{ParseException, SimpleDateFormat}
import java.util.Date

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.model.ArchiveLink
import nz.co.searchwellington.model.frontend.FrontendResource
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

  private val dateFormatter = new DateFormatter(DateTimeZone.UTC) // TODO use global
  private val pathMonthParser = new SimpleDateFormat("yyyy MMM")

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/archive/.*?/.*?$")
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {
    if (isValid(request)) {
      getArchiveMonthFromPath(request.getPathInfo).map { month =>
        val mv = new ModelAndView
        val monthLabel = dateFormatter.fullMonthYear(month.getStart.toDate)
        mv.addObject("heading", monthLabel)
        mv.addObject("description", "Archived newsitems for the month of " + monthLabel)
        import scala.collection.JavaConverters._
        mv.addObject(MAIN_CONTENT, Await.result(contentRetrievalService.getNewsitemsForInterval(month, Option(loggedInUserFilter.getLoggedInUser)), TenSeconds).asJava)
      }

    } else {
      None
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView): Unit = {
    getArchiveMonthFromPath(request.getPathInfo).map { month =>
      val eventualArchiveMonths = contentRetrievalService.getArchiveMonths(Option(loggedInUserFilter.getLoggedInUser))
      val eventualArchiveCounts = contentRetrievalService.getArchiveCounts(Option(loggedInUserFilter.getLoggedInUser))
      val eventualMonthPublishers = contentRetrievalService.getPublishersForInterval(month, Option(loggedInUserFilter.getLoggedInUser))

      val eventuallyPopulated = for {
        archiveLinks <- eventualArchiveMonths
        archiveStatistics <- eventualArchiveCounts
        monthPublishers: Seq[(FrontendResource, Long)] <- eventualMonthPublishers
      } yield {
        populateNextAndPreviousLinks(mv, month, archiveLinks)
        archiveLinksService.populateArchiveLinks(mv, archiveLinks, archiveStatistics)
        import scala.collection.JavaConverters._
        mv.addObject("publishers", monthPublishers.map(_._1.name).asJava)
      }

      Await.result(eventuallyPopulated, ThirtySeconds)
    }
  }

  def getViewName(mv: ModelAndView): String = "archivePage"

  private def populateNextAndPreviousLinks(mv: ModelAndView, month: Interval, archiveLinks: Seq[ArchiveLink]) {
    var selected: ArchiveLink = null
    import scala.collection.JavaConversions._
    for (link <- archiveLinks) {
      if (link.getMonth == month.getStart.toDate) {
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

  private def getArchiveMonthFromPath(path: String): Option[Interval] = {
    def intervalForMonth(month: Date): Interval = {
      new Interval(new DateTime(month), new DateTime(month).plusMonths(1))
    }

    if (path.startsWith("/archive/")) {
      val fields = path.split("/")
      if (fields.length == 4) {
        val archiveMonthString = fields(2) + " " + fields(3)
        try {
          val month = pathMonthParser.parse(archiveMonthString)
          Some(intervalForMonth(month))
        }
        catch {
          case e: ParseException => {
            log.warn("Could not parse archive month; ignoring: " + archiveMonthString, e)
            None
          }
        }
      } else {
        None
      }
    } else {
      None
    }
  }

}
