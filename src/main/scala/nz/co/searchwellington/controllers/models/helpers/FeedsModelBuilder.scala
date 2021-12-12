package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.suggesteditems.SuggestedFeeditemsService
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.{FeedAcceptancePolicy, User}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

@Component class FeedsModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                suggestedFeeditemsService: SuggestedFeeditemsService,
                                                urlBuilder: UrlBuilder,
                                                commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder
  with ReasonableWaits {

  private val log = LogFactory.getLog(classOf[FeedsModelBuilder])

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
      val mv = new ModelAndView().
        addObject("heading", "Feeds").
        addObject("description", "Incoming feeds").
        addObject("link", urlBuilder.fullyQualified(urlBuilder.getFeedsUrl)).
        addObject(MAIN_CONTENT, feeds.asJava)
      Some(mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = {
    val eventualSuggestedFeedNewsitems = {
      if (loggedInUser.exists(_.isAdmin)) {
        suggestedFeeditemsService.getSuggestionFeednewsitems(6, loggedInUser)
      } else {
        Future.successful(Seq.empty)
      }
    }
    val eventualDiscoveredFeedOccurrences = {
      if (loggedInUser.exists(_.isAdmin)) {
        contentRetrievalService.getDiscoveredFeeds(maxDiscoveredFeedsToShow)
      } else {
        Future.successful(Seq.empty)
      }
    }
    val eventualCurrentFeeds = contentRetrievalService.getAllFeedsOrderedByLatestItemDate(loggedInUser)

    for {
      suggestedFeednewsitems <- eventualSuggestedFeedNewsitems
      discoveredFeedOccurrences <- eventualDiscoveredFeedOccurrences
      currentFeeds <- eventualCurrentFeeds

    } yield {
      mv.addObject("suggestions", suggestedFeednewsitems.asJava)
      mv.addObject("discovered_feeds", discoveredFeedOccurrences.asJava)
      // TODO make conditional
      mv.addObject("discovered_feeds_moreurl", urlBuilder.getDiscoveredFeeds)
      commonAttributesModelBuilder.withSecondaryFeeds(mv, currentFeeds)
    }
  }

  def getViewName(mv: ModelAndView, loggedInUser: Option[User]): String = "feeds"

}
