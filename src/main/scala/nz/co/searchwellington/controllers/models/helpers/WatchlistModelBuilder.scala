package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

@Component class WatchlistModelBuilder @Autowired()(var contentRetrievalService: ContentRetrievalService, var rssUrlBuilder: RssUrlBuilder, var urlBuilder: UrlBuilder, var commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder {

  private val log = Logger.getLogger(classOf[WatchlistModelBuilder])

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/watchlist(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {
    if (isValid(request)) {
      log.debug("Building watchlist page model")
      val mv = new ModelAndView
      mv.addObject("heading", "News watchlist")
      mv.addObject("description", "The news watchlist")
      mv.addObject("link", urlBuilder.getWatchlistUrl)

      import scala.collection.JavaConverters._
      mv.addObject(MAIN_CONTENT, contentRetrievalService.getAllWatchlists.asJava)

      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForJustin, rssUrlBuilder.getRssUrlForWatchlist)
      Some(mv)

    } else {
      None
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
  }

  def getViewName(mv: ModelAndView): String = {
    "watchlist"
  }

}
