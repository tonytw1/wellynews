package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.SuggestedFeeditemsService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest

@Component class SuggestionsModelBuilder @Autowired()(suggestedFeeditemsService: SuggestedFeeditemsService,
                                                      rssUrlBuilder: RssUrlBuilder,
                                                      urlBuilder: UrlBuilder,
                                                      contentRetrievalService: ContentRetrievalService,
                                                      commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder {

  private val log = Logger.getLogger(classOf[SuggestionsModelBuilder])
  private val MAX_SUGGESTIONS = 50

  def isValid(request: HttpServletRequest): Boolean = {
    return request.getPathInfo.matches("^/feeds/inbox(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {
    if (isValid(request)) {
      val mv = new ModelAndView
      mv.addObject(MAIN_CONTENT, suggestedFeeditemsService.getSuggestionFeednewsitems(MAX_SUGGESTIONS))
      mv.addObject("heading", "Inbox")
      mv.addObject("link", urlBuilder.getFeedsInboxUrl)
      mv.addObject("description", "Suggested newsitems from local feeds.")
      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getTitleForSuggestions, rssUrlBuilder.getRssUrlForFeedSuggestions)
      Some(mv)
    } else {
      None
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    commonAttributesModelBuilder.populateSecondaryFeeds(mv)
  }

  def getViewName(mv: ModelAndView): String = {
    return "suggestions"
  }
}