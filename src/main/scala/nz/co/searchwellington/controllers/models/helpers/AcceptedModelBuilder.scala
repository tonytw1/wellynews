package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

@Component class AcceptedModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                   rssUrlBuilder: RssUrlBuilder, val urlBuilder: UrlBuilder,
                                                   commonAttributesModelBuilder: CommonAttributesModelBuilder)
  extends ModelBuilder with CommonSizes with ReasonableWaits with Pagination {

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches("^/accepted(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {
    val page = getPage(request)

    for {
      acceptedNewsitmes <- contentRetrievalService.getAcceptedNewsitems(MAX_NEWSITEMS, loggedInUser = loggedInUser, page = page)
    } yield {
      val mv = new ModelAndView().
        addObject("heading", "Accepted").
        addObject("description", "The most recently accepted feed news items.").
        addObject("link", urlBuilder.fullyQualified(urlBuilder.getAcceptedUrl)).
        addObject(MAIN_CONTENT, acceptedNewsitmes._1.asJava)

      val startIndex = getStartIndex(page, MAX_NEWSITEMS)
      def paginationLinks(page: Int): String = {
        urlBuilder.getAcceptedUrl + "?page=" + page
      }
      populatePagination(mv, startIndex, acceptedNewsitmes._2, MAX_NEWSITEMS, paginationLinks)
      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForAccepted, rssUrlBuilder.getRssUrlForAccepted)
      Some(mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = {
    withLatestNewsitems(mv, loggedInUser)
  }

  def getViewName(mv: ModelAndView, loggedInUser: Option[User]): String = "accepted"

}
