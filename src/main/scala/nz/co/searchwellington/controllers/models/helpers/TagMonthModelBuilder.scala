package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.{Tag, TagArchiveLink, User}
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.joda.time.Interval
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.dates.DateFormatter

import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

@Component class TagMonthModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                   dateFormatter: DateFormatter, rssUrlBuilder: RssUrlBuilder)
  extends ModelBuilder with ArchiveMonth {

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

  override def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {
    val tags = request.getAttribute(TAGS).asInstanceOf[Seq[Tag]]

    tags.headOption.map { tag =>
      parseMonth(tag, RequestPath.getPathFrom(request)).map { month =>
        for {
          newsitemsForMonth <- contentRetrievalService.getNewsitemsForTagInterval(tag, month, loggedInUser)
        } yield {
          Some(new ModelAndView().
            addObject("tag", tag).
            addObject(MAIN_CONTENT, newsitemsForMonth.asJava).
            addObject("heading", tag.getDisplayName + " - " + dateFormatter.fullMonthYear(month.getStart.toDate)).
            addObject("month", month).
            addObject("rss_url", rssUrlBuilder.getRssUrlForTag(tag)))
        }

      }.getOrElse {
        Future.successful(None)
      }

    }.getOrElse {
      Future.successful(None)
    }
  }

  override def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = {
    val tag = request.getAttribute("tag").asInstanceOf[Tag]
    for {
      archiveLinks <- contentRetrievalService.getTagArchiveMonths(tag, loggedInUser)
    } yield {
      val tagArchiveLinks = archiveLinks.map { a =>
        TagArchiveLink(tag = tag, interval = a.interval, count = a.count)
      }
      mv.addObject("tag_archive_links", tagArchiveLinks.asJava)
    }
    Future.successful(mv)
  }

  override def getViewName(mv: ModelAndView, loggedInUser: Option[User]): String = "publisherMonth"

  private def parseMonth(tag: Tag, path: String): Option[Interval] = {
    if (path.startsWith("/" + tag.name)) {
        val last = path.split("/").last
      parseYearMonth(last)
    } else {
      None
      }
  }

}
