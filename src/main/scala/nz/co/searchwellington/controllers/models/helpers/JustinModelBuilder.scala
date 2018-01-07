package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

@Component class JustinModelBuilder @Autowired() (contentRetrievalService: ContentRetrievalService, rssUrlBuilder: RssUrlBuilder, urlBuilder: UrlBuilder, commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder with CommonSizes {

  def isValid(request: HttpServletRequest): Boolean = {
      return request.getPathInfo.matches("^/justin(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {
    if (isValid(request)) {
      val mv: ModelAndView = new ModelAndView
      mv.addObject("heading", "Latest additions")
      mv.addObject("description", "The most recently submitted website listings.")
      mv.addObject("link", urlBuilder.getJustinUrl)
      val latestSites = contentRetrievalService.getLatestWebsites(MAX_NEWSITEMS)
      mv.addObject(MAIN_CONTENT, latestSites)
      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForJustin, rssUrlBuilder.getRssUrlForJustin)
      Some(mv)
    }
    None
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
  }

  def getViewName(mv: ModelAndView): String = {
    return "justin"
  }

}
