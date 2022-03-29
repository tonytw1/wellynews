package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
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

@Component class JustinModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                 rssUrlBuilder: RssUrlBuilder, val urlBuilder: UrlBuilder,
                                                 commonAttributesModelBuilder: CommonAttributesModelBuilder)
  extends ModelBuilder with CommonSizes with ReasonableWaits with Pagination {

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches("^/justin(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {
    val page = getPage(request)

    for {
      websites <- contentRetrievalService.getLatestWebsites(MAX_NEWSITEMS, loggedInUser = loggedInUser, page = page)
    } yield {
      val mv = new ModelAndView().
        addObject("heading", "Latest additions").
        addObject("description", "The most recently submitted website listings.").
        addObject("link", urlBuilder.fullyQualified(urlBuilder.getJustinUrl)).
        addObject(MAIN_CONTENT, websites._1.asJava)

      val startIndex = getStartIndex(page, MAX_NEWSITEMS)
      def paginationLinks(page: Int): String = {
        urlBuilder.getJustinUrl + "?page=" + page
      }
      populatePagination(mv, startIndex, websites._2, MAX_NEWSITEMS, paginationLinks)

      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForJustin, rssUrlBuilder.getRssUrlForJustin)
      Some(mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User]): Future[ModelMap] = {
    latestNewsitems(loggedInUser)
  }

  def getViewName(mv: ModelAndView, loggedInUser: Option[User]): String = "justin"

}
