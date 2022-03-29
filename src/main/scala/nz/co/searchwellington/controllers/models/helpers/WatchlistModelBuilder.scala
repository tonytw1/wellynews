package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

@Component class WatchlistModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                    rssUrlBuilder: RssUrlBuilder,
                                                    urlBuilder: UrlBuilder,
                                                    commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder
  with Pagination with CommonSizes {

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches("^/watchlist(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {
    val page = getPage(request)
    for {
      watchlists <- contentRetrievalService.getWatchlistItems(loggedInUser, page = page)
    } yield {
      val mv = new ModelAndView().
        addObject("heading", "News watchlist").
        addObject("description", "The news watchlist").
        addObject("link", urlBuilder.fullyQualified(urlBuilder.getWatchlistUrl)).
        addObject(MAIN_CONTENT, watchlists._1.asJava)
      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForJustin, rssUrlBuilder.getRssUrlForWatchlist)

      val startIndex = getStartIndex(page, MAX_NEWSITEMS)
      def paginationLinks(page: Int): String = {
        urlBuilder.getWatchlistUrl + "?page=" + page
      }
      populatePagination(mv, startIndex, watchlists._2, MAX_NEWSITEMS, paginationLinks)

      Some(mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User]): Future[ModelMap] = {
    latestNewsitems(loggedInUser)
  }

  def getViewName(mv: ModelAndView, loggedInUser: Option[User]): String = "watchlist"

}
