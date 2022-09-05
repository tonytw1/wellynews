package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.{AcceptedDay, User}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.joda.time.LocalDate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap

import javax.servlet.http.HttpServletRequest
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Component class AcceptedModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                   rssUrlBuilder: RssUrlBuilder, val urlBuilder: UrlBuilder,
                                                   commonAttributesModelBuilder: CommonAttributesModelBuilder)
  extends ModelBuilder with CommonSizes with ReasonableWaits with Pagination with ArchiveMonths {

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches("^/accepted(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]] = {
    val date = Option(request.getParameter("date")).map { dateString =>
      new LocalDate(dateString)
    }
    for {
      acceptedNewsitemsAndTotalCount <- contentRetrievalService.getAcceptedNewsitems(MAX_NEWSITEMS, loggedInUser = loggedInUser, acceptedDate = date)
    } yield {
      val acceptedNewsitems = acceptedNewsitemsAndTotalCount._1
      val totalItems = acceptedNewsitemsAndTotalCount._2

      val mv = new ModelMap().
        addAttribute("heading", "Accepted").
        addAttribute("description", "The most recently accepted feed news items.").
        addAttribute("link", urlBuilder.fullyQualified(urlBuilder.getAcceptedUrl)).
        addAttribute(MAIN_CONTENT, acceptedNewsitems.asJava)

      if (totalItems > MAX_NEWSITEMS) {
        dayOfLastItem(acceptedNewsitems).foreach { i =>
          mv.addAttribute("more", urlBuilder.getAcceptedUrl(i))
        }
      }
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
          val day = java.time.LocalDate.parse(dateString)
          AcceptedDay(day, count)
      }
    }
  }

}
