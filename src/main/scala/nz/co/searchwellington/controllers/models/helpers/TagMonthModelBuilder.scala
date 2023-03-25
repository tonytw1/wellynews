package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.{Tag, TagArchiveLink, User}
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.joda.time.Interval
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap
import uk.co.eelpieconsulting.common.dates.DateFormatter

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Component class TagMonthModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                   dateFormatter: DateFormatter, rssUrlBuilder: RssUrlBuilder)
  extends ModelBuilder with ArchiveMonth with ArchiveMonths {

  private val TAGS = "tags"

  override def isValid(request: HttpServletRequest): Boolean = {
    val tags = request.getAttribute(TAGS).asInstanceOf[Seq[Tag]]
    if (tags != null && tags.size == 1) {
      tags.headOption.flatMap { tag =>
        parseMonth(tag, RequestPath.getPathFrom(request))
      }.nonEmpty
    } else {
      false
    }
  }

  override def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]] = {
    val tags = request.getAttribute(TAGS).asInstanceOf[Seq[Tag]]

    tags.headOption.map { tag =>
      parseMonth(tag, RequestPath.getPathFrom(request)).map { month =>
        for {
          newsitemsForMonth <- contentRetrievalService.getNewsitemsForTagInterval(tag, month, loggedInUser)
        } yield {
          Some(new ModelMap().
            addAttribute("tag", tag).
            addAttribute(MAIN_CONTENT, newsitemsForMonth.asJava).
            addAttribute("heading", tag.getDisplayName + " - " + dateFormatter.fullMonthYear(month.getStart.toDate)).
            addAttribute("month", month).
            addAttribute("rss_url", rssUrlBuilder.getRssUrlForTag(tag)))
        }

      }.getOrElse {
        Future.successful(None)
      }

    }.getOrElse {
      Future.successful(None)
    }
  }

  override def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[ModelMap] = {
    val tags = request.getAttribute(TAGS).asInstanceOf[Seq[Tag]]
    val mv = new ModelMap()
    tags.headOption.map { tag =>
      for {
        archiveLinks <- contentRetrievalService.getTagArchiveMonths(tag, loggedInUser)
      } yield {
        val tagArchiveLinks = archiveLinks.map { a =>
          TagArchiveLink(tag = tag, interval = a.interval, count = a.count)
        }
        mv.addAttribute("archive_links", tagArchiveLinks.asJava)
        parseMonth(tag, RequestPath.getPathFrom(request)).foreach { month =>
          mv.addAllAttributes(populateNextAndPreviousLinks(month, tagArchiveLinks))
        }
        mv
      }
    }.getOrElse(
      Future.successful(mv)
    )
  }

  override def getViewName(mv: ModelMap, loggedInUser: Option[User]): String = "tagMonth"

  private def parseMonth(tag: Tag, path: String): Option[Interval] = {
    val tagPrefix = "/" + tag.name + "/"
    if (path.startsWith(tagPrefix)) {
      val remaining = path.replaceFirst(tagPrefix, "")
      val yearMonth = remaining.split("/").headOption
      yearMonth.flatMap(parseYearMonth)
    } else {
      None
      }
  }

}
