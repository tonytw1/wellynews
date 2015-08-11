package nz.co.searchwellington.controllers.models.helpers

import java.util.List
import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.controllers.models.helpers.CommonAttributesModelBuilder
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

@Component object JustinModelBuilder {
  private var log: Logger = Logger.getLogger(classOf[JustinModelBuilder])
}

@Component class JustinModelBuilder extends ModelBuilder {
  private var contentRetrievalService: ContentRetrievalService = null
  private var rssUrlBuilder: RssUrlBuilder = null
  private var urlBuilder: UrlBuilder = null
  private var commonAttributesModelBuilder: CommonAttributesModelBuilder = null

  @Autowired def this(contentRetrievalService: ContentRetrievalService, rssUrlBuilder: RssUrlBuilder, urlBuilder: UrlBuilder, commonAttributesModelBuilder: CommonAttributesModelBuilder) {
    this()
    this.contentRetrievalService = contentRetrievalService
    this.rssUrlBuilder = rssUrlBuilder
    this.urlBuilder = urlBuilder
    this.commonAttributesModelBuilder = commonAttributesModelBuilder
  }

  def isValid(request: HttpServletRequest): Boolean = {
    return request.getPathInfo.matches("^/justin(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest): ModelAndView = {
    if (isValid(request)) {
      JustinModelBuilder.log.debug("Building justin page model")
      val mv: ModelAndView = new ModelAndView
      mv.addObject("heading", "Latest additions")
      mv.addObject("description", "The most recently submitted website listings.")
      mv.addObject("link", urlBuilder.getJustinUrl)
      val latestSites: List[FrontendResource] = contentRetrievalService.getLatestWebsites(CommonAttributesModelBuilder.MAX_NEWSITEMS)
      mv.addObject("main_content", latestSites)
      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForJustin, rssUrlBuilder.getRssUrlForJustin)
      return mv
    }
    return null
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
  }

  def getViewName(mv: ModelAndView): String = {
    return "justin"
  }
}