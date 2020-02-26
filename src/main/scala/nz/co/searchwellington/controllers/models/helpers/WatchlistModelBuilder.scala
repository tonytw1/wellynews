package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RssUrlBuilder}
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class WatchlistModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService,
                                                    rssUrlBuilder: RssUrlBuilder,
                                                    urlBuilder: UrlBuilder,
                                                    commonAttributesModelBuilder: CommonAttributesModelBuilder,
                                                    loggedInUserFilter: LoggedInUserFilter) extends ModelBuilder {

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/watchlist(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest): Future[Option[ModelAndView]] = {
    if (isValid(request)) {
      for {
        watchlists <- contentRetrievalService.getAllWatchlists(Option(loggedInUserFilter.getLoggedInUser))
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
    } else {
      Future.successful(None)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
  }

  def getViewName(mv: ModelAndView): String = "watchlist"

}
