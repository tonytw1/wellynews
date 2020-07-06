package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.helpers.ArchiveLinksService
import nz.co.searchwellington.model.{ArchiveLink, PublisherArchiveLink, User}
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.joda.time.{DateTimeZone, Interval}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.dates.DateFormatter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class ArchiveModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService, archiveLinksService: ArchiveLinksService) extends
  ModelBuilder with ReasonableWaits with ArchiveMonth {

  private val dateFormatter = new DateFormatter(DateTimeZone.UTC) // TODO use global

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

  def getViewName(mv: ModelAndView): String = "archivePage"

  private def populateNextAndPreviousLinks(mv: ModelAndView, month: Interval, archiveLinks: Seq[ArchiveLink]) {
    var selected: ArchiveLink = null
    import scala.collection.JavaConversions._
    for (link <- archiveLinks) {
      if (link.interval == month) {
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
    if (path.matches(archiveMonthPath)) {
      val fields = path.split("/")
      parseYearMonth(fields.last)
    } else {
      None
    }
  }

}
