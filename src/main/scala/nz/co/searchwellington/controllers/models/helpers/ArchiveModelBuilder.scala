package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.helpers.ArchiveLinksService
import nz.co.searchwellington.model.{ArchiveLink, PublisherArchiveLink, User}
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.joda.time.{DateTimeZone, Interval}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.dates.DateFormatter

import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class ArchiveModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService, archiveLinksService: ArchiveLinksService,
                                                  dateFormatter: DateFormatter) extends
  ModelBuilder with ReasonableWaits with ArchiveMonth {

  private val archiveMonthPath = "^/archive/.*?$"

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches(archiveMonthPath)
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {
    getArchiveMonthFromPath(RequestPath.getPathFrom(request)).map { month =>
      for {
        newsitemsForMonth <- contentRetrievalService.getNewsitemsForInterval(month, loggedInUser)
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

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = {
    withLatestNewsitems(mv, loggedInUser).flatMap { mv =>
      getArchiveMonthFromPath(RequestPath.getPathFrom(request)).map { month =>
        val eventualArchiveLinks = contentRetrievalService.getArchiveMonths(loggedInUser)
        val eventualArchiveCounts = contentRetrievalService.getArchiveCounts(loggedInUser)
        val eventualMonthPublishers = contentRetrievalService.getPublishersForInterval(month, loggedInUser)

        for {
          archiveLinks <- eventualArchiveLinks
          archiveStatistics <- eventualArchiveCounts
          monthPublishers <- eventualMonthPublishers
        } yield {
          populateNextAndPreviousLinks(mv, month, archiveLinks)
          archiveLinksService.populateArchiveLinks(mv, archiveLinks, archiveStatistics)

          val publisherArchiveLinks = monthPublishers.map { i =>
            PublisherArchiveLink(i._1, month, i._2)
          }

          import scala.collection.JavaConverters._
          mv.addObject("publisher_archive_links", publisherArchiveLinks.asJava)
        }

      }.getOrElse {
        Future.successful(mv)
      }
    }
  }

  def getViewName(mv: ModelAndView, loggedInUser: Option[User]): String = "archivePage"

  private def populateNextAndPreviousLinks(mv: ModelAndView, currentMonth: Interval, archiveLinks: Seq[ArchiveLink]) {
    val previousMonth = currentMonth.withStart(currentMonth.getStart.minusMonths(1)).withEnd(currentMonth.getStart)
    val nextMonth = currentMonth.withStart(currentMonth.getStart.plusMonths(1)).withEnd(currentMonth.getStart.plusMonths(2))
    archiveLinks.find { link =>
      link.interval == previousMonth
    }.map { l =>
      mv.addObject("previous_month", l)
    }
    archiveLinks.find { link =>
      link.interval == nextMonth
    }.map { l =>
      mv.addObject("next_month", l)
    }
  }

  private def getArchiveMonthFromPath(path: String): Option[Interval] = {
    if (path.matches(archiveMonthPath)) {
      val fields = path.split("/")
      parseYearMonth(fields.last)
    } else {
      None
    }
  }

}
