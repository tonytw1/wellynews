package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Component class WatchlistModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                    rssUrlBuilder: RssUrlBuilder,
                                                    urlBuilder: UrlBuilder,
                                                    commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder
  with Pagination with CommonSizes {

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches("^/watchlist(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]] = {
    val page = getPage(request)
    for {
      watchlists <- contentRetrievalService.getWatchlistItems(loggedInUser, page = page)
    } yield {
      val mv = new ModelMap().
        addAttribute("heading", "News watchlist").
        addAttribute("description", "The news watchlist").
        addAttribute("link", urlBuilder.fullyQualified(urlBuilder.getWatchlistUrl)).
        addAttribute(MAIN_CONTENT, watchlists._1.asJava)
      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForJustin, rssUrlBuilder.getRssUrlForWatchlist)

      val startIndex = getStartIndex(page, MAX_NEWSITEMS)
      def paginationLinks(page: Int): String = {
        urlBuilder.getWatchlistUrl + "?page=" + page
      }
      populatePagination(mv, startIndex, watchlists._2, MAX_NEWSITEMS, paginationLinks)

      Some(mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[ModelMap] = {
    latestNewsitems(loggedInUser)
  }

  def getViewName(mv: ModelMap, loggedInUser: Option[User]): String = "watchlist"

}
