package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.{RssUrlBuilder, UrlBuilder}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Component class JustinModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                 rssUrlBuilder: RssUrlBuilder, val urlBuilder: UrlBuilder,
                                                 commonAttributesModelBuilder: CommonAttributesModelBuilder)
  extends ModelBuilder with CommonSizes with ReasonableWaits with Pagination {

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches("^/justin(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]] = {
    val page = getPage(request)

    for {
      websites <- contentRetrievalService.getLatestWebsites(MAX_NEWSITEMS, loggedInUser = loggedInUser, page = page)
    } yield {
      val mv = new ModelMap().
        addAttribute("heading", "Latest additions").
        addAttribute("description", "The most recently submitted website listings.").
        addAttribute("link", urlBuilder.fullyQualified(urlBuilder.getJustinUrl)).
        addAttribute(MAIN_CONTENT, websites._1.asJava)

      val startIndex = getStartIndex(page, MAX_NEWSITEMS)
      def paginationLinks(page: Int): String = {
        urlBuilder.getJustinUrl + "?page=" + page
      }
      populatePagination(mv, startIndex, websites._2, MAX_NEWSITEMS, paginationLinks)

      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForJustin, rssUrlBuilder.getRssUrlForJustin)
      Some(mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[ModelMap] = {
    latestNewsitems(loggedInUser)
  }

  def getViewName(mv: ModelMap, loggedInUser: Option[User]): String = "justin"

}
