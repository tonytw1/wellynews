package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.model.FeedAcceptancePolicy
import nz.co.searchwellington.repositories.{ContentRetrievalService, SuggestedFeeditemsService}
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Component class FeedsModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService, suggestedFeeditemsService: SuggestedFeeditemsService,
                                                urlBuilder: UrlBuilder,
                                                commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder with ReasonableWaits {

  private val log = Logger.getLogger(classOf[FeedsModelBuilder])

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/feeds(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {
    if (isValid(request)) {
      log.info("Building feed page model")
      val mv: ModelAndView = new ModelAndView
      mv.addObject("heading", "Feeds")
      mv.addObject("description", "Incoming feeds")
      mv.addObject("link", urlBuilder.getFeedsUrl)

      import scala.collection.JavaConverters._
      val withAcceptancePolicy = Option(request.getParameter("acceptance")).map(FeedAcceptancePolicy.valueOf)
      mv.addObject(MAIN_CONTENT, Await.result(contentRetrievalService.getFeeds(withAcceptancePolicy), TenSeconds).asJava)
      Some(mv)

    } else {
      None
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    val eventualSuggestedFeednewsitems = suggestedFeeditemsService.getSuggestionFeednewsitems(6)
    val eventualDiscoveredFeeds = contentRetrievalService.getDiscoveredFeeds

    Await.result(commonAttributesModelBuilder.populateSecondaryFeeds(mv).map { mv =>
      import scala.collection.JavaConverters._
      mv.addObject("suggestions", Await.result(eventualSuggestedFeednewsitems, TenSeconds).asJava)
      mv.addObject("discovered_feeds", Await.result(eventualDiscoveredFeeds, TenSeconds).asJava)
    }, TenSeconds)
  }

  def getViewName(mv: ModelAndView): String = {
    "feeds"
  }

}
