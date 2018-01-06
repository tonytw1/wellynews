package nz.co.searchwellington.controllers

import java.io.IOException
import java.util.List
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import nz.co.searchwellington.annotations.Timed
import nz.co.searchwellington.feeds.DiscoveredFeedRepository
import nz.co.searchwellington.model.ArchiveLink
import nz.co.searchwellington.repositories.{ContentRetrievalService, TagDAO}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller class SimplePageController @Autowired() (discoveredFeedRepository: DiscoveredFeedRepository, tagDAO: TagDAO, rssUrlBuilder: RssUrlBuilder, commonModelObjectsService: CommonModelObjectsService, urlStack: UrlStack, contentRetrievalService: ContentRetrievalService) {

  @RequestMapping(Array("/about"))
  @Timed(timingNotes = "")
  @throws[IOException]
  def about(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView
    urlStack.setUrlStack(request)
    commonModelObjectsService.populateCommonLocal(mv)
    mv.addObject("heading", "About")
    mv.setViewName("about")
    mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5, 1))
    mv
  }

  @RequestMapping(Array("/archive"))
  @throws[IOException]
  def archive(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView
    urlStack.setUrlStack(request)
    commonModelObjectsService.populateCommonLocal(mv)
    mv.addObject("heading", "Archive")
    val archiveMonths: List[ArchiveLink] = contentRetrievalService.getArchiveMonths
    mv.addObject("archiveLinks", archiveMonths)
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
    mv.addObject("feeds", contentRetrievalService.getAllFeeds)
    mv.addObject("publishers", contentRetrievalService.getAllPublishers)
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
    mv.addObject("feedable_tags", contentRetrievalService.getFeedworthyTags)
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
    val nonCommentFeeds = discoveredFeedRepository.getAllNonCommentDiscoveredFeeds
    mv.addObject("discovered_feeds", nonCommentFeeds)
    mv.setViewName("discoveredFeeds")
    mv
  }

  @RequestMapping(Array("/publishers")) def publishers(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView
    urlStack.setUrlStack(request)
    commonModelObjectsService.populateCommonLocal(mv)
    mv.addObject("heading", "All Publishers")
    mv.addObject("publishers", contentRetrievalService.getAllPublishers)
    mv.setViewName("publishers")
    mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5, 1))
    mv
  }

  @RequestMapping(Array("/signin")) def signin(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView
    commonModelObjectsService.populateCommonLocal(mv)
    mv.addObject("heading", "Sign in")
    mv.setViewName("signin")
    mv
  }

}