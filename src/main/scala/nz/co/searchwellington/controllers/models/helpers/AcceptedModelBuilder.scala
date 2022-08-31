package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.{AcceptedDay, User}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap

import java.time.LocalDate
import javax.servlet.http.HttpServletRequest
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Component class AcceptedModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                   rssUrlBuilder: RssUrlBuilder, val urlBuilder: UrlBuilder,
                                                   commonAttributesModelBuilder: CommonAttributesModelBuilder)
  extends ModelBuilder with CommonSizes with ReasonableWaits with Pagination {

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches("^/accepted(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]] = {
    val page = getPage(request)

    for {
      acceptedNewsitmes <- contentRetrievalService.getAcceptedNewsitems(MAX_NEWSITEMS, loggedInUser = loggedInUser, page = page)
    } yield {
      val mv = new ModelMap().
        addAttribute("heading", "Accepted").
        addAttribute("description", "The most recently accepted feed news items.").
        addAttribute("link", urlBuilder.fullyQualified(urlBuilder.getAcceptedUrl)).
        addAttribute(MAIN_CONTENT, acceptedNewsitmes._1.asJava)

      val startIndex = getStartIndex(page, MAX_NEWSITEMS)
      def paginationLinks(page: Int): String = {
        urlBuilder.getAcceptedUrl + "?page=" + page
      }
      populatePagination(mv, startIndex, acceptedNewsitmes._2, MAX_NEWSITEMS, paginationLinks)
      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForAccepted, rssUrlBuilder.getRssUrlForAccepted)
      Some(mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[ModelMap] = {
    for {
      latestNewsitems <- latestNewsitems(loggedInUser)
      acceptedDays <- getAcceptedDays(loggedInUser)
    } yield {
      new ModelMap().addAllAttributes(latestNewsitems).addAttribute("acceptedDays", acceptedDays.asJava)
    }
  }

  def getViewName(mv: ModelMap, loggedInUser: Option[User]): String = "accepted"

  private def getAcceptedDays(loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span) = {
    for {
      acceptedDatesAggregation <- contentRetrievalService.getAcceptedDates(loggedInUser)
    } yield {
      acceptedDatesAggregation.take(14).map {
        case (dateString, count) =>
          val day = LocalDate.parse(dateString)
          AcceptedDay(day, count)
      }
    }
  }

}
