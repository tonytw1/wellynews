package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.helpers.ArchiveLinksService
import nz.co.searchwellington.model.{PublisherArchiveLink, User}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.RssUrlBuilder
import org.joda.time.Interval
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap
import uk.co.eelpieconsulting.common.dates.DateFormatter

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Component class ArchiveModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService, archiveLinksService: ArchiveLinksService,
                                                  dateFormatter: DateFormatter, rssUrlBuilder: RssUrlBuilder) extends
  ModelBuilder with ReasonableWaits with ArchiveMonth with ArchiveMonths {

  private val archiveMonthPath = "^/archive/.*?$"

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches(archiveMonthPath)
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]] = {
    getArchiveMonthFromPath(RequestPath.getPathFrom(request)).map { month =>
      for {
        newsitemsForMonth <- contentRetrievalService.getNewsitemsForInterval(month, loggedInUser)
      } yield {
        val monthLabel = dateFormatter.fullMonthYear(month.getStart.toDate)
        Some(new ModelMap().
          addAttribute("heading", monthLabel).
          addAttribute("description", "Archived newsitems for the month of " + monthLabel).
          addAttribute(MAIN_CONTENT, newsitemsForMonth.asJava).
          addAttribute("rss_url", rssUrlBuilder.getBaseRssUrl))
      }
    }.getOrElse {
      Future.successful(None)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[ModelMap] = {
    latestNewsitems(loggedInUser).flatMap { mv =>
      getArchiveMonthFromPath(RequestPath.getPathFrom(request)).map { month =>
        val eventualArchiveLinks = contentRetrievalService.getArchiveMonths(loggedInUser)
        val eventualArchiveCounts = contentRetrievalService.getArchiveTypeCounts(loggedInUser)
        val eventualMonthPublishers = contentRetrievalService.getPublishersForInterval(month, loggedInUser)

        for {
          archiveLinks <- eventualArchiveLinks
          archiveStatistics <- eventualArchiveCounts
          monthPublishers <- eventualMonthPublishers
        } yield {
          mv.addAllAttributes(populateNextAndPreviousLinks(month, archiveLinks))
          mv.addAllAttributes(archiveLinksService.populateArchiveLinks(archiveLinks, archiveStatistics.toMap))
          val publisherArchiveLinks = monthPublishers.map { i =>
            PublisherArchiveLink(i._1, month, Some(i._2))
          }
          mv.addAttribute("publisher_archive_links", publisherArchiveLinks.asJava)
        }

      }.getOrElse {
        Future.successful(mv)
      }
    }
  }

  def getViewName(mv: ModelMap, loggedInUser: Option[User]): String = "archivePage"

  private def getArchiveMonthFromPath(path: String): Option[Interval] = {
    if (path.matches(archiveMonthPath)) {
      val fields = path.split("/")
      parseYearMonth(fields.last)
    } else {
      None
    }
  }

}
