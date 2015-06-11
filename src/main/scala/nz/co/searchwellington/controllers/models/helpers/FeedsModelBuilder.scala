package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.controllers.models.helpers.CommonAttributesModelBuilder
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.SuggestedFeeditemsService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

@Component object FeedsModelBuilder {
  private[models] var log: Logger = Logger.getLogger(classOf[FeedsModelBuilder])
}

@Component class FeedsModelBuilder extends ModelBuilder {
  private var suggestedFeeditemsService: SuggestedFeeditemsService = null
  private var urlBuilder: UrlBuilder = null
  private var contentRetrievalService: ContentRetrievalService = null
  private var commonAttributesModelBuilder: CommonAttributesModelBuilder = null

  @Autowired def this(contentRetrievalService: ContentRetrievalService, suggestedFeeditemsService: SuggestedFeeditemsService, urlBuilder: UrlBuilder, commonAttributesModelBuilder: CommonAttributesModelBuilder) {
    this()
    this.contentRetrievalService = contentRetrievalService
    this.suggestedFeeditemsService = suggestedFeeditemsService
    this.urlBuilder = urlBuilder
    this.contentRetrievalService = contentRetrievalService
    this.commonAttributesModelBuilder = commonAttributesModelBuilder
  }

  def isValid(request: HttpServletRequest): Boolean = {
    return request.getPathInfo.matches("^/feeds(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest): ModelAndView = {
    if (isValid(request)) {
      FeedsModelBuilder.log.debug("Building feed page model")
      val mv: ModelAndView = new ModelAndView
      mv.addObject("heading", "Feeds")
      mv.addObject("description", "Incoming feeds")
      mv.addObject("link", urlBuilder.getFeedsUrl)
      mv.addObject("main_content", contentRetrievalService.getAllFeeds)
      return mv
    }
    return null
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    commonAttributesModelBuilder.populateSecondaryFeeds(mv)
    mv.addObject("suggestions", suggestedFeeditemsService.getSuggestionFeednewsitems(6))
    mv.addObject("discovered_feeds", contentRetrievalService.getDiscoveredFeeds)
  }

  def getViewName(mv: ModelAndView): String = {
    return "feeds"
  }
}