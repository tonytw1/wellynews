package nz.co.searchwellington.controllers

import java.io.IOException

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
  @throws[IOException]
  def about(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView
    urlStack.setUrlStack(request)
    commonModelObjectsService.populateCommonLocal(mv)
    mv.addObject("heading", "About")
    mv.setViewName("about")
    import scala.collection.JavaConverters._
    mv.addObject("latest_newsitems", Await.result(contentRetrievalService.getLatestNewsitems(5, loggedInUser = Option(loggedInUserFilter.getLoggedInUser)), TenSeconds).asJava)
    mv
  }

  @RequestMapping(Array("/archive"))
  @throws[IOException]
  def archive(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView
    urlStack.setUrlStack(request)
    commonModelObjectsService.populateCommonLocal(mv)
    mv.addObject("heading", "Archive")
    import scala.collection.JavaConverters._
    mv.addObject("archiveLinks", Await.result(contentRetrievalService.getArchiveMonths(Option(loggedInUserFilter.getLoggedInUser)), TenSeconds).asJava)
    mv.setViewName("archiveIndex")
    mv
  }

  @RequestMapping(Array("/api"))
  @throws[IOException]
  def api(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView
    urlStack.setUrlStack(request)
    commonModelObjectsService.populateCommonLocal(mv)
    mv.addObject("heading", "The Wellynews API")
    mv.addObject("feeds", contentRetrievalService.getFeeds(loggedInUser = Option(loggedInUserFilter.getLoggedInUser)))
    mv.addObject("publishers", contentRetrievalService.getAllPublishers(Option(loggedInUserFilter.getLoggedInUser)))
    mv.addObject("api_tags", contentRetrievalService.getTopLevelTags)
    mv.setViewName("api")
    mv
  }

  @RequestMapping(Array("/rssfeeds"))
  @throws[IOException]
  def rssfeeds(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView
    urlStack.setUrlStack(request)
    commonModelObjectsService.populateCommonLocal(mv)
    mv.addObject("heading", "RSS feeds")
    setRss(mv, rssUrlBuilder.getBaseRssTitle, rssUrlBuilder.getBaseRssUrl)
    mv.addObject("feedable_tags", contentRetrievalService.getFeedworthyTags(Option(loggedInUserFilter.getLoggedInUser)))
    mv.setViewName("rssfeeds")
    mv
  }

  protected def setRss(mv: ModelAndView, title: String, url: String) {
    mv.addObject("rss_title", title)
    mv.addObject("rss_url", url)
  }

  @RequestMapping(Array("/feeds/discovered"))
  @throws[IOException]
  def discovered(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView
    commonModelObjectsService.populateCommonLocal(mv)
    urlStack.setUrlStack(request)
    mv.addObject("heading", "Discovered Feeds")
    import scala.collection.JavaConverters._
    mv.addObject("discovered_feeds", Await.result(mongoRepository.getAllDiscoveredFeeds, TenSeconds).asJava)
    mv.setViewName("discoveredFeeds")
    mv
  }

  @RequestMapping(Array("/publishers")) def publishers(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView
    urlStack.setUrlStack(request)
    commonModelObjectsService.populateCommonLocal(mv)
    mv.addObject("heading", "All Publishers")

    import scala.collection.JavaConverters._
    val publishers = Await.result(contentRetrievalService.getAllPublishers(Option(loggedInUserFilter.getLoggedInUser)), TenSeconds).sortBy(_.title).map(p => frontendResourceMapper.createFrontendResourceFrom(p))
    mv.addObject("publishers", publishers.asJava)

    mv.setViewName("publishers")
    mv.addObject("latest_newsitems", Await.result(contentRetrievalService.getLatestNewsitems(5, loggedInUser = Option(loggedInUserFilter.getLoggedInUser)), TenSeconds).asJava)
    mv
  }

}
