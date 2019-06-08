package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RssUrlBuilder}
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.repositories.{ContentRetrievalService, SuggestedFeeditemsService}
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Component class SuggestionsModelBuilder @Autowired()(suggestedFeeditemsService: SuggestedFeeditemsService,
                                                      rssUrlBuilder: RssUrlBuilder,
                                                      urlBuilder: UrlBuilder,
                                                      contentRetrievalService: ContentRetrievalService,
                                                      commonAttributesModelBuilder: CommonAttributesModelBuilder, loggedInUserFilter: LoggedInUserFilter) extends ModelBuilder
  with ReasonableWaits {

  private val log = Logger.getLogger(classOf[SuggestionsModelBuilder])
  private val MAX_SUGGESTIONS = 50

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/feeds/inbox(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {
    if (isValid(request)) {
      val mv = new ModelAndView
      import scala.collection.JavaConverters._
      mv.addObject(MAIN_CONTENT, Await.result(suggestedFeeditemsService.getSuggestionFeednewsitems(MAX_SUGGESTIONS), TenSeconds).asJava)
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
    Await.result(contentRetrievalService.getAllFeedsOrderByLatestItemDate(Option(loggedInUserFilter.getLoggedInUser)).map { feeds =>
      commonAttributesModelBuilder.populateSecondaryFeeds(mv, feeds)
    }, TenSeconds)
  }

  def getViewName(mv: ModelAndView): String = {
    "suggestions"
  }

}
