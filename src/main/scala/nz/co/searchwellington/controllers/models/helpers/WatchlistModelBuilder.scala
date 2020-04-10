package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
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

@Component class WatchlistModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                    rssUrlBuilder: RssUrlBuilder,
                                                    urlBuilder: UrlBuilder,
                                                    commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder {

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/watchlist(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {
    for {
      watchlists <- contentRetrievalService.getAllWatchlists(loggedInUser)
    } yield {
      import scala.collection.JavaConverters._
      val mv = new ModelAndView().
        addObject("heading", "News watchlist").
        addObject("description", "The news watchlist").
        addObject("link", urlBuilder.getWatchlistUrl).
        addObject(MAIN_CONTENT, watchlists.asJava)
      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForJustin, rssUrlBuilder.getRssUrlForWatchlist)
      Some(mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = {
    withLatestNewsitems(mv, loggedInUser)
  }

  def getViewName(mv: ModelAndView): String = "watchlist"

}
