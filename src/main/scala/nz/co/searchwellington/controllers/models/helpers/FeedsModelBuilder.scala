package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.model.{FeedAcceptancePolicy, User}
import nz.co.searchwellington.repositories.{ContentRetrievalService, SuggestedFeeditemsService}
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class FeedsModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                suggestedFeeditemsService: SuggestedFeeditemsService,
                                                urlBuilder: UrlBuilder,
                                                commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder with ReasonableWaits {

  private val log = Logger.getLogger(classOf[FeedsModelBuilder])

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/feeds(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: User): Future[Option[ModelAndView]] = {
    log.info("Building feed page model")
    val withAcceptancePolicy = Option(request.getParameter("acceptance")).map(FeedAcceptancePolicy.valueOf)
    for {
      feeds <- contentRetrievalService.getFeeds(withAcceptancePolicy, Option(loggedInUser))
    } yield {
      val mv = new ModelAndView().
        addObject("heading", "Feeds").
        addObject("description", "Incoming feeds").
        addObject("link", urlBuilder.getFeedsUrl)
      import scala.collection.JavaConverters._
      mv.addObject(MAIN_CONTENT, feeds.asJava)
      Some(mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = {
    val eventualSuggestedFeednewsitems = Future.successful(Seq.empty) // TODO suggestedFeeditemsService.getSuggestionFeednewsitems(6)
    val eventualDiscoveredFeeds = contentRetrievalService.getDiscoveredFeeds
    val eventualCurrentFeeds = contentRetrievalService.getAllFeedsOrderedByLatestItemDate(loggedInUser)

    for {
      suggestedFeednewsitems <- eventualSuggestedFeednewsitems
      discoveredFeeds <- eventualDiscoveredFeeds
      currentFeeds <- eventualCurrentFeeds

    } yield {
      import scala.collection.JavaConverters._
      mv.addObject("suggestions", suggestedFeednewsitems.asJava)
      mv.addObject("discovered_feeds", discoveredFeeds.asJava)
      commonAttributesModelBuilder.withSecondaryFeeds(mv, currentFeeds)
    }
  }

  def getViewName(mv: ModelAndView): String = "feeds"

}
