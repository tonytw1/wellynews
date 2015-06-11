package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.repositories.{ContentRetrievalService, SuggestedFeeditemsService}
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

@Component object FeedsModelBuilder {
  private[models] var log: Logger = Logger.getLogger(classOf[FeedsModelBuilder])
}

@Component class FeedsModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService, suggestedFeeditemsService: SuggestedFeeditemsService,
                                                urlBuilder: UrlBuilder,
                                                commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder {

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