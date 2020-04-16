package nz.co.searchwellington.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{ContentRetrievalService, TagDAO}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

@Order(1)
@Controller class SimplePageController @Autowired()(tagDAO: TagDAO, rssUrlBuilder: RssUrlBuilder,
                                                    urlStack: UrlStack,
                                                    val contentRetrievalService: ContentRetrievalService,
                                                    frontendResourceMapper: FrontendResourceMapper,
                                                    mongoRepository: MongoRepository, loggedInUserFilter: LoggedInUserFilter)
  extends ReasonableWaits with CommonModelObjectsService {

  @RequestMapping(value = Array("/about"), method = Array(RequestMethod.GET)) def about(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    urlStack.setUrlStack(request)

    import scala.collection.JavaConverters._
    Await.result((for {
      latestNewsitems <- contentRetrievalService.getLatestNewsitems(5, loggedInUser = loggedInUserFilter.getLoggedInUser)
    } yield {
      new ModelAndView("about").
        addObject("heading", "About").
        addObject("latest_newsitems", latestNewsitems.asJava)
    }).flatMap(withCommonLocal), TenSeconds)
  }

  @RequestMapping(Array("/archive"))
  def archive(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
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
  def api(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    urlStack.setUrlStack(request)

    Await.result(withCommonLocal(new ModelAndView("api").
      addObject("heading", "The Wellynews API").
      addObject("feeds", contentRetrievalService.getFeeds(loggedInUser = loggedInUserFilter.getLoggedInUser)).
      addObject("publishers", contentRetrievalService.getAllPublishers(loggedInUserFilter.getLoggedInUser)).
      addObject("api_tags", contentRetrievalService.getTopLevelTags)), TenSeconds)
  }

  @RequestMapping(Array("/rssfeeds"))
  def rssfeeds(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    urlStack.setUrlStack(request)

    Await.result(withCommonLocal(new ModelAndView("rssfeeds").
      addObject("heading", "RSS feeds").
      addObject("feedable_tags", contentRetrievalService.getFeedworthyTags(loggedInUserFilter.getLoggedInUser))), TenSeconds)
  }

  @RequestMapping(Array("/feeds/discovered"))
  def discovered(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    urlStack.setUrlStack(request)

    import scala.collection.JavaConverters._
    Await.result(withCommonLocal(new ModelAndView("discoveredFeeds").
      addObject("heading", "Discovered Feeds").
      addObject("discovered_feeds", Await.result(mongoRepository.getAllDiscoveredFeeds, TenSeconds).asJava)), TenSeconds)
  }

  // TODO duplication
  def withLatestNewsitems(mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = {
    for {
      latestNewsitems <- contentRetrievalService.getLatestNewsitems(5, loggedInUser = loggedInUser)
    } yield {
      import scala.collection.JavaConverters._
      mv.addObject("latest_newsitems", latestNewsitems.asJava)
    }
  }

}
