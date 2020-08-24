package nz.co.searchwellington.controllers

import javax.servlet.http.HttpServletRequest
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
      latestNewsitems <- contentRetrievalService.getLatestNewsitems(5, loggedInUser = loggedInUserFilter.getLoggedInUser)
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
      addObject("heading", "The Wellynews API").
      addObject("feeds", contentRetrievalService.getFeeds(loggedInUser = loggedInUserFilter.getLoggedInUser)).
      addObject("publishers", contentRetrievalService.getAllPublishers(loggedInUserFilter.getLoggedInUser)).  // TODO Future!?
      addObject("api_tags", contentRetrievalService.getTopLevelTags)), TenSeconds)
  }

  @RequestMapping(Array("/rssfeeds"))
  def rssfeeds(request: HttpServletRequest): ModelAndView = {
    urlStack.setUrlStack(request)

    Await.result(withCommonLocal{
      import scala.collection.JavaConverters._
      val mv  = new ModelAndView("rssfeeds").
        addObject("heading", "RSS feeds").
        addObject("feedable_tags", Await.result(contentRetrievalService.getFeedworthyTags(), TenSeconds).asJava)

      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getBaseRssTitle, rssUrlBuilder.getBaseRssUrl)
      mv
    }, TenSeconds)
  }

  @RequestMapping(Array("/feeds/discovered"))
  def discovered(request: HttpServletRequest): ModelAndView = {
    urlStack.setUrlStack(request)

    import scala.collection.JavaConverters._
    Await.result(withCommonLocal(new ModelAndView("discoveredFeeds").
      addObject("heading", "Discovered Feeds").
      addObject("discovered_feeds", Await.result(mongoRepository.getAllDiscoveredFeeds, TenSeconds).asJava)), TenSeconds)
  }

}
