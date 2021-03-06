package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.{ContentRetrievalService, SuggestedFeeditemsService}
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class SuggestionsModelBuilder @Autowired()(suggestedFeeditemsService: SuggestedFeeditemsService,
                                                      rssUrlBuilder: RssUrlBuilder,
                                                      urlBuilder: UrlBuilder,
                                                      val contentRetrievalService: ContentRetrievalService,
                                                      commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder
  with ReasonableWaits {

  private val MAX_SUGGESTIONS = 50

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches("^/feeds/inbox(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {
    for {
      suggestions <- suggestedFeeditemsService.getSuggestionFeednewsitems(MAX_SUGGESTIONS, loggedInUser)
    } yield {
      import scala.collection.JavaConverters._
      val mv = new ModelAndView().
        addObject(MAIN_CONTENT, suggestions.asJava).
        addObject("heading", "Inbox").
        addObject("link", urlBuilder.fullyQualified(urlBuilder.getFeedsInboxUrl)).
        addObject("description", "Suggested news items from local feeds.")
      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getTitleForSuggestions, rssUrlBuilder.getRssUrlForFeedSuggestions)
      Some(mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = {
    for {
      suggestOnlyFeeds <- contentRetrievalService.getSuggestOnlyFeeds(loggedInUser)
    } yield {
      commonAttributesModelBuilder.withSecondaryFeeds(mv, suggestOnlyFeeds, "Suggest only feeds", "Newsitems from these feeds are not automatically accepted.")
    }
  }

  def getViewName(mv: ModelAndView) = "suggestions"

}
