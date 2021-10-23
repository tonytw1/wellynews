package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.{FeedAcceptancePolicy, User}
import nz.co.searchwellington.repositories.{ContentRetrievalService, SuggestedFeeditemsService}
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class FeedsModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                suggestedFeeditemsService: SuggestedFeeditemsService,
                                                urlBuilder: UrlBuilder,
                                                commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder
  with ReasonableWaits with DiscoveredFeeds {

  private val log = Logger.getLogger(classOf[FeedsModelBuilder])

  private val maxDiscoveredFeedsToShow = 10

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches("^/feeds(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {
    log.info("Building feed page model")
    val withAcceptancePolicy = Option(request.getParameter("acceptance")).map(FeedAcceptancePolicy.valueOf)
    for {
      feeds <- contentRetrievalService.getFeeds(withAcceptancePolicy, loggedInUser)
    } yield {
      import scala.collection.JavaConverters._
      val mv = new ModelAndView().
        addObject("heading", "Feeds").
        addObject("description", "Incoming feeds").
        addObject("link", urlBuilder.fullyQualified(urlBuilder.getFeedsUrl)).
        addObject(MAIN_CONTENT, feeds.asJava)
      Some(mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = {
    val eventualDiscoveredFeedOccurrences = contentRetrievalService.getDiscoveredFeeds
    val eventualCurrentFeeds = contentRetrievalService.getAllFeedsOrderedByLatestItemDate(loggedInUser)
    val eventualSuggestedFeedNewsitems = suggestedFeeditemsService.getSuggestionFeednewsitems(6, loggedInUser)

    for {
      suggestedFeednewsitems <- eventualSuggestedFeedNewsitems
      discoveredFeedOccurrences <- eventualDiscoveredFeedOccurrences
      currentFeeds <- eventualCurrentFeeds

    } yield {
      import scala.collection.JavaConverters._
      mv.addObject("suggestions", suggestedFeednewsitems.asJava)
      val discoveredFeeds = filterDiscoveredFeeds(discoveredFeedOccurrences)
      mv.addObject("discovered_feeds", discoveredFeeds.take(maxDiscoveredFeedsToShow).asJava)
      if (discoveredFeeds.length > maxDiscoveredFeedsToShow) {
        mv.addObject("discovered_feeds_moreurl", urlBuilder.getDiscoveredFeeds())
      }
      commonAttributesModelBuilder.withSecondaryFeeds(mv, currentFeeds)
    }
  }

  def getViewName(mv: ModelAndView): String = "feeds"

}
