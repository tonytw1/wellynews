package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.feeds.suggesteditems.{SuggestedFeedsService, SuggestedFeeditemsService}
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

@Component class SuggestionsModelBuilder @Autowired()(suggestedFeeditemsService: SuggestedFeeditemsService,
                                                      suggestedFeedsService: SuggestedFeedsService,
                                                      rssUrlBuilder: RssUrlBuilder, urlBuilder: UrlBuilder,
                                                      val contentRetrievalService: ContentRetrievalService,
                                                      commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder with ReasonableWaits {

  private val MAX_SUGGESTIONS = 50

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches("^/feeds/inbox(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {
    for {
      suggestions <- suggestedFeeditemsService.getSuggestionFeednewsitems(MAX_SUGGESTIONS, loggedInUser)
    } yield {
      val mv = new ModelAndView().
        addObject(MAIN_CONTENT, suggestions.asJava).
        addObject("heading", "Inbox").
        addObject("link", urlBuilder.fullyQualified(urlBuilder.getFeedsInboxUrl)).
        addObject("description", "Suggested news items from local feeds.")
      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getTitleForSuggestions, rssUrlBuilder.getRssUrlForFeedSuggestions)
      Some(mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User]): Future[ModelMap] = {
    for {
      inboxFeeds <- suggestedFeedsService.getSuggestedFeedsOrderedByLatestFeeditemDate()
    } yield {
      new ModelMap().addAttribute("righthand_heading", "Suggest only feeds")
        .addAttribute("righthand_description", "Newsitems from these feeds are not automatically accepted.")
        .addAttribute("righthand_content", inboxFeeds.asJava)
    }
  }

  def getViewName(mv: ModelAndView, loggedInUser: Option[User]): String = "suggestions"

}
