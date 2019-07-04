package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RssUrlBuilder}
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.Await

@Component class JustinModelBuilder @Autowired() (contentRetrievalService: ContentRetrievalService,
                                                  rssUrlBuilder: RssUrlBuilder, urlBuilder: UrlBuilder, commonAttributesModelBuilder: CommonAttributesModelBuilder,
                                                  loggedInUserFilter: LoggedInUserFilter)
  extends ModelBuilder with CommonSizes with ReasonableWaits {

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/justin(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {
    if (isValid(request)) {
      val mv: ModelAndView = new ModelAndView
      mv.addObject("heading", "Latest additions")
      mv.addObject("description", "The most recently submitted website listings.")
      mv.addObject("link", urlBuilder.getJustinUrl)
      import scala.collection.JavaConverters._
      mv.addObject(MAIN_CONTENT, Await.result(contentRetrievalService.getLatestWebsites(MAX_NEWSITEMS, loggedInUser = Option(loggedInUserFilter.getLoggedInUser)), TenSeconds).asJava)
      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForJustin, rssUrlBuilder.getRssUrlForJustin)
      Some(mv)

    } else {
      None
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
  }

  def getViewName(mv: ModelAndView) = "justin"
}
