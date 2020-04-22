package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class JustinModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                 rssUrlBuilder: RssUrlBuilder, val urlBuilder: UrlBuilder,
                                                 commonAttributesModelBuilder: CommonAttributesModelBuilder)
  extends ModelBuilder with CommonSizes with ReasonableWaits with Pagination {

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/justin(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {
    for {
      websites <- contentRetrievalService.getLatestWebsites(MAX_NEWSITEMS, loggedInUser = loggedInUser)
    } yield {
      import scala.collection.JavaConverters._
      val mv = new ModelAndView().
        addObject("heading", "Latest additions").
        addObject("description", "The most recently submitted website listings.").
        addObject("link", urlBuilder.getJustinUrl).
        addObject(MAIN_CONTENT, websites._1.asJava)


      val page = getPage(request)
      val startIndex = getStartIndex(page, MAX_NEWSITEMS)

      populatePagination(mv, startIndex, websites._2, MAX_NEWSITEMS)

      def paginationLinks(page: Int): String = {
        urlBuilder.getJustinUrl + "?page=" + page
      }
      mv.addObject("page_links", makePaginationLinks(startIndex, websites._2, MAX_NEWSITEMS, paginationLinks))

      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForJustin, rssUrlBuilder.getRssUrlForJustin)
      Some(mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = {
    withLatestNewsitems(mv, loggedInUser)
  }

  def getViewName(mv: ModelAndView) = "justin"

}
