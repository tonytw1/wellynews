package nz.co.searchwellington.controllers

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.helpers.CommonAttributesModelBuilder
import nz.co.searchwellington.model.SiteInformation
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.RssUrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters._

@Order(1)
@Controller class SimplePageController @Autowired()( siteInformation: SiteInformation,
                                                     urlStack: UrlStack,
                                                     val contentRetrievalService: ContentRetrievalService,
                                                     mongoRepository: MongoRepository,
                                                     loggedInUserFilter: LoggedInUserFilter,
                                                     rssUrlBuilder: RssUrlBuilder,
                                                     commonAttributesModelBuilder: CommonAttributesModelBuilder)
  extends ReasonableWaits with CommonModelObjectsService {

  @GetMapping(value = Array("/about")) def about(request: HttpServletRequest): ModelAndView = {
    implicit val currentSpan: Span = Span.current()
    urlStack.setUrlStack(request)
    val loggedInUser = loggedInUserFilter.getLoggedInUser
    val mv = new ModelAndView("about").
      addObject("heading", "About").
      addObject("loggedInUser", loggedInUser.orNull).
      addObject("user_agent", siteInformation.getUserAgent)

    val eventualModelAndView = for {
      commonLocal <- commonLocal
      latestNewsitems <- latestNewsitems(loggedInUser)

    } yield {
      mv.addAllObjects(commonLocal)
      mv.addAllObjects(latestNewsitems)
    }

    Await.result(eventualModelAndView, TenSeconds)
  }

  @GetMapping(Array("/archive"))
  def archive(request: HttpServletRequest): ModelAndView = {
    implicit val currentSpan: Span = Span.current()
    urlStack.setUrlStack(request)
    val loggedInUser = loggedInUserFilter.getLoggedInUser

    Await.result(for {
      links <- contentRetrievalService.getArchiveMonths(loggedInUser)
      commonLocal <- commonLocal
      latestNewsitems <- latestNewsitems(loggedInUser)
    } yield {
      new ModelAndView("archiveIndex").
        addObject("heading", "Archive").
        addObject("loggedInUser", loggedInUser.orNull).
        addObject("archiveLinks", links.asJava).addAllObjects(commonLocal).addAllObjects(latestNewsitems)
    }, TenSeconds)
  }

  @GetMapping(Array("/api"))
  def api(request: HttpServletRequest): ModelAndView = {
    urlStack.setUrlStack(request)
    val loggedInUser = loggedInUserFilter.getLoggedInUser.orNull

    Await.result(for {
      commonLocal <- commonLocal
    } yield {
      new ModelAndView("api").
        addObject("loggedInUser", loggedInUser).
        addObject("heading", "The Wellynews API").addAllObjects(commonLocal)
    }, TenSeconds)
  }

  @GetMapping(Array("/rssfeeds"))
  def rssfeeds(request: HttpServletRequest): ModelAndView = {
    implicit val currentSpan: Span = Span.current()
    urlStack.setUrlStack(request)
    val loggedInUser = loggedInUserFilter.getLoggedInUser

    val eventualModelAndView = for {
      exampleFeedableTags <- contentRetrievalService.getFeaturedTags
      exampleFeedablePublishers <- contentRetrievalService.getFeaturedPublishers(loggedInUser)
      commonLocal <- commonLocal
      latestNewsitems <- latestNewsitems(loggedInUser)

    } yield {
        val mv = new ModelMap().
          addAttribute("heading", "RSS feeds").
          addAttribute("loggedInUser", loggedInUser.orNull).
          addAttribute("feedable_publishers", exampleFeedablePublishers.asJava).
          addAttribute("feedable_tags", exampleFeedableTags.asJava).
          addAllAttributes(commonLocal).
          addAllAttributes(latestNewsitems)
        commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getBaseRssTitle, rssUrlBuilder.getBaseRssUrl)
        new ModelAndView("rssfeeds").addAllObjects(mv)
    }

    Await.result(eventualModelAndView, TenSeconds)
  }

  @GetMapping(Array("/feeds/discovered"))
  def discovered(request: HttpServletRequest): ModelAndView = {
    implicit val currentSpan: Span = Span.current()
    urlStack.setUrlStack(request)
    val loggedInUser = loggedInUserFilter.getLoggedInUser

    val eventualDiscoveredFeeds = mongoRepository.getDiscoveredFeeds(1000)
    val eventualFeeds = contentRetrievalService.getAllFeedsOrderedByLatestItemDate(loggedInUser)

    val eventualModelAndView = for {
      discoveredFeeds <- eventualDiscoveredFeeds
      feeds <- eventualFeeds
      commonLocal <- commonLocal
    } yield {
      new ModelAndView("discoveredFeeds").
        addObject("heading", "Discovered Feeds").
        addObject("loggedInUser", loggedInUser.orNull).
        addObject("discovered_feeds", discoveredFeeds.asJava).
        addAllObjects(commonLocal).
        addAllObjects(commonAttributesModelBuilder.secondaryFeeds(feeds))
    }

    Await.result(eventualModelAndView, TenSeconds)
  }

}
