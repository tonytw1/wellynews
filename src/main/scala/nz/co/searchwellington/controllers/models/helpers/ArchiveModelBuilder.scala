package nz.co.searchwellington.controllers.models.helpers

import java.text.{ParseException, SimpleDateFormat}
import java.util.Date

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.helpers.ArchiveLinksService
import nz.co.searchwellington.model.{ArchiveLink, User}
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.apache.log4j.Logger
import org.joda.time.{DateTime, DateTimeZone, Interval}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.dates.DateFormatter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class ArchiveModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService, archiveLinksService: ArchiveLinksService) extends
  ModelBuilder with ReasonableWaits {

  private val log = Logger.getLogger(classOf[ArchiveModelBuilder])

  private val dateFormatter = new DateFormatter(DateTimeZone.UTC) // TODO use global
  private val pathMonthParser = new SimpleDateFormat("yyyy MMM")

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/archive/.*?/.*?$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: User): Future[Option[ModelAndView]] = {
    getArchiveMonthFromPath(request.getPathInfo).map { month =>
      for {
        newsitemsForMonth <- contentRetrievalService.getNewsitemsForInterval(month, Option(loggedInUser))
      } yield {
        val monthLabel = dateFormatter.fullMonthYear(month.getStart.toDate)
        import scala.collection.JavaConverters._
        Some(new ModelAndView().
          addObject("heading", monthLabel).
          addObject("description", "Archived newsitems for the month of " + monthLabel).
          addObject(MAIN_CONTENT, newsitemsForMonth.asJava))
      }
    }.getOrElse {
      Future.successful(None)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: User): Future[ModelAndView] = {
    getArchiveMonthFromPath(request.getPathInfo).map { month =>
      val eventualArchiveMonths = contentRetrievalService.getArchiveMonths(Option(loggedInUser))
      val eventualArchiveCounts = contentRetrievalService.getArchiveCounts(Option(loggedInUser))
      val eventualMonthPublishers = contentRetrievalService.getPublishersForInterval(month, Option(loggedInUser))

      for {
        archiveLinks <- eventualArchiveMonths
        archiveStatistics <- eventualArchiveCounts
        monthPublishers <- eventualMonthPublishers
      } yield {
        populateNextAndPreviousLinks(mv, month, archiveLinks)
        archiveLinksService.populateArchiveLinks(mv, archiveLinks, archiveStatistics)

        import scala.collection.JavaConverters._
        mv.addObject("publishers", monthPublishers.map(_._1).asJava)
      }

    }.getOrElse{
      Future.successful(mv)
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
