package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.helpers.CommonAttributesModelBuilder
import nz.co.searchwellington.model.SiteInformation
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Order(1)
@Controller class SimplePageController @Autowired()( siteInformation: SiteInformation,
                                                     urlStack: UrlStack,
                                                     val contentRetrievalService: ContentRetrievalService,
                                                     mongoRepository: MongoRepository,
                                                     loggedInUserFilter: LoggedInUserFilter,
                                                     rssUrlBuilder: RssUrlBuilder,
                                                     commonAttributesModelBuilder: CommonAttributesModelBuilder)
  extends ReasonableWaits with CommonModelObjectsService {

  @RequestMapping(value = Array("/about"), method = Array(RequestMethod.GET)) def about(request: HttpServletRequest): ModelAndView = {
    urlStack.setUrlStack(request)

    import scala.collection.JavaConverters._
    Await.result((for {
      latestNewsitems <- eventualLatestNewsitems
    } yield {
      new ModelAndView("about").
        addObject("heading", "About").
        addObject("user_agent", siteInformation.getUserAgent).
        addObject("latest_newsitems", latestNewsitems.asJava)
    }).flatMap(withCommonLocal), TenSeconds)
  }

  @RequestMapping(Array("/archive"))
  def archive(request: HttpServletRequest): ModelAndView = {
    urlStack.setUrlStack(request)
    val loggedInUser = loggedInUserFilter.getLoggedInUser

    Await.result((for {
      links <- contentRetrievalService.getArchiveMonths(loggedInUser)
    } yield {
      import scala.collection.JavaConverters._
      new ModelAndView("archiveIndex").
        addObject("heading", "Archive").
        addObject("archiveLinks", links.asJava)
    }).flatMap(withCommonLocal).flatMap(mv => withLatestNewsitems(mv, loggedInUser)), TenSeconds)
  }

  @RequestMapping(Array("/api"))
  def api(request: HttpServletRequest): ModelAndView = {
    urlStack.setUrlStack(request)
    Await.result(withCommonLocal(new ModelAndView("api").
      addObject("heading", "The Wellynews API")), TenSeconds)
  }

  @RequestMapping(Array("/rssfeeds"))
  def rssfeeds(request: HttpServletRequest): ModelAndView = {
    urlStack.setUrlStack(request)
    val loggedInUser = loggedInUserFilter.getLoggedInUser

    Await.result(withLatestNewsitems(Await.result(withCommonLocal {
      import scala.collection.JavaConverters._
      val mv = new ModelAndView("rssfeeds").
        addObject("heading", "RSS feeds").
        addObject("feedable_tags", Await.result(contentRetrievalService.getFeaturedTags, TenSeconds).asJava)

      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getBaseRssTitle, rssUrlBuilder.getBaseRssUrl)
      mv
    }, TenSeconds), loggedInUser), TenSeconds)
  }

  @RequestMapping(Array("/feeds/discovered"))
  def discovered(request: HttpServletRequest): ModelAndView = {
    urlStack.setUrlStack(request)
    val loggedInUser = loggedInUserFilter.getLoggedInUser

    val discoveredFeeds = Await.result(mongoRepository.getDiscoveredFeeds(1000), TenSeconds)
    val feeds = Await.result(contentRetrievalService.getAllFeedsOrderedByLatestItemDate(loggedInUser), TenSeconds)

    import scala.collection.JavaConverters._
    val mv = Await.result(withCommonLocal(new ModelAndView("discoveredFeeds").
      addObject("heading", "Discovered Feeds").
      addObject("discovered_feeds", discoveredFeeds.asJava)), TenSeconds)
    commonAttributesModelBuilder.withSecondaryFeeds(mv, feeds)
  }

  private def eventualLatestNewsitems = contentRetrievalService.getLatestNewsitems(5, loggedInUser = loggedInUserFilter.getLoggedInUser)

}
