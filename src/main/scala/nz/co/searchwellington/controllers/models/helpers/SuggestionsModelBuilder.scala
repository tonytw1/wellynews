package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.{ContentRetrievalService, SuggestedFeeditemsService}
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class SuggestionsModelBuilder @Autowired()(suggestedFeeditemsService: SuggestedFeeditemsService,
                                                      rssUrlBuilder: RssUrlBuilder,
                                                      urlBuilder: UrlBuilder,
                                                      contentRetrievalService: ContentRetrievalService,
                                                      commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder
  with ReasonableWaits {

  private val log = Logger.getLogger(classOf[SuggestionsModelBuilder])
  private val MAX_SUGGESTIONS = 50

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/feeds/inbox(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: User): Future[Option[ModelAndView]] = {
    if (isValid(request)) {
      for {
        suggestions <- suggestedFeeditemsService.getSuggestionFeednewsitems(MAX_SUGGESTIONS)
      } yield {
        import scala.collection.JavaConverters._
        val mv = new ModelAndView().
          addObject(MAIN_CONTENT, suggestions.asJava).
          addObject("heading", "Inbox").
          addObject("link", urlBuilder.getFeedsInboxUrl).
          addObject("description", "Suggested newsitems from local feeds.")
        commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getTitleForSuggestions, rssUrlBuilder.getRssUrlForFeedSuggestions)
        Some(mv)
      }

    } else {
      Future.successful(None)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: User): Future[ModelAndView] = {
    for {
      allFeeds <- contentRetrievalService.getAllFeedsOrderedByLatestItemDate(Option(loggedInUser))
    } yield {
      commonAttributesModelBuilder.withSecondaryFeeds(mv, allFeeds)
    }
  }

  def getViewName(mv: ModelAndView): String = "suggestions"

}
