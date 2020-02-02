package nz.co.searchwellington.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.annotations.Timed
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{ContentRetrievalService, TagDAO}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller class SimplePageController @Autowired()(tagDAO: TagDAO, rssUrlBuilder: RssUrlBuilder,
                                                    commonModelObjectsService: CommonModelObjectsService, urlStack: UrlStack,
                                                    contentRetrievalService: ContentRetrievalService, frontendResourceMapper: FrontendResourceMapper,
                                                    mongoRepository: MongoRepository, loggedInUserFilter: LoggedInUserFilter) extends ReasonableWaits {

  @RequestMapping(Array("/about"))
  @Timed(timingNotes = "")
  def about(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    urlStack.setUrlStack(request)

    import scala.collection.JavaConverters._
    commonModelObjectsService.withCommonLocal(new ModelAndView("about").
      addObject("heading", "About").
      addObject("latest_newsitems", Await.result(contentRetrievalService.getLatestNewsitems(5, loggedInUser = Option(loggedInUserFilter.getLoggedInUser)), TenSeconds).asJava))
  }

  @RequestMapping(Array("/archive"))
  def archive(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    urlStack.setUrlStack(request)

    import scala.collection.JavaConverters._
    commonModelObjectsService.withCommonLocal(new ModelAndView("archiveIndex").
      addObject("heading", "Archive").
      addObject("archiveLinks", Await.result(contentRetrievalService.getArchiveMonths(Option(loggedInUserFilter.getLoggedInUser)), TenSeconds).asJava))
  }

  @RequestMapping(Array("/api"))
  def api(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    urlStack.setUrlStack(request)

    val mv = new ModelAndView("api").
      addObject("heading", "The Wellynews API").
      addObject("feeds", contentRetrievalService.getFeeds(loggedInUser = Option(loggedInUserFilter.getLoggedInUser))).
      addObject("publishers", contentRetrievalService.getAllPublishers(Option(loggedInUserFilter.getLoggedInUser))).
      addObject("api_tags", contentRetrievalService.getTopLevelTags)
    commonModelObjectsService.withCommonLocal(mv)
  }

  @RequestMapping(Array("/rssfeeds"))
  def rssfeeds(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    urlStack.setUrlStack(request)

    commonModelObjectsService.withCommonLocal(new ModelAndView("rssfeeds").
      addObject("heading", "RSS feeds").
      addObject("feedable_tags", contentRetrievalService.getFeedworthyTags(Option(loggedInUserFilter.getLoggedInUser))))
  }

  @RequestMapping(Array("/feeds/discovered"))
  def discovered(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    urlStack.setUrlStack(request)

    import scala.collection.JavaConverters._
    commonModelObjectsService.withCommonLocal(new ModelAndView("discoveredFeeds").
      addObject("heading", "Discovered Feeds").
      addObject("discovered_feeds", Await.result(mongoRepository.getAllDiscoveredFeeds, TenSeconds).asJava))
  }

}
