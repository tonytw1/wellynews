package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.suggesteditems.{SuggestedFeeditemsService, SuggestedFeedsService}
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.{RssUrlBuilder, UrlBuilder}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap

import scala.concurrent.{ExecutionContext, Future}
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

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]] = {
    for {
      suggestions <- suggestedFeeditemsService.getSuggestionFeednewsitems(MAX_SUGGESTIONS, loggedInUser)
    } yield {
      val mv = new ModelMap().
        addAttribute(MAIN_CONTENT, suggestions.asJava).
        addAttribute("heading", "Inbox").
        addAttribute("link", urlBuilder.fullyQualified(urlBuilder.getFeedsInboxUrl)).
        addAttribute("description", "Suggested news items from local feeds.")
      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getTitleForSuggestions, rssUrlBuilder.getRssUrlForFeedSuggestions)
      Some(mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[ModelMap] = {
    for {
      inboxFeeds <- suggestedFeedsService.getSuggestedFeedsOrderedByLatestFeeditemDate()
    } yield {
      new ModelMap().addAttribute("righthand_heading", "Suggest only feeds")
        .addAttribute("righthand_description", "Newsitems from these feeds are not automatically accepted.")
        .addAttribute("righthand_content", inboxFeeds.asJava)
    }
  }

  def getViewName(mv: ModelMap, loggedInUser: Option[User]): String = "suggestions"

}
