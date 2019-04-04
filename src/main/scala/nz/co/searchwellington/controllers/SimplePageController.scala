package nz.co.searchwellington.controllers

import java.io.IOException

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.annotations.Timed
import nz.co.searchwellington.feeds.DiscoveredFeedRepository
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{ContentRetrievalService, TagDAO}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}

@Controller class SimplePageController @Autowired() (discoveredFeedRepository: DiscoveredFeedRepository, tagDAO: TagDAO, rssUrlBuilder: RssUrlBuilder,
                                                     commonModelObjectsService: CommonModelObjectsService, urlStack: UrlStack,
                                                     contentRetrievalService: ContentRetrievalService, frontendResourceMapper: FrontendResourceMapper,
                                                     mongoRepository: MongoRepository, loggedInUserFilter: LoggedInUserFilter) {

  private val tenSeconds = Duration(10, SECONDS)

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
    mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5, 1).asJava)
    mv
  }

  @RequestMapping(Array("/profiles"))
  @Timed(timingNotes = "") def all(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv: ModelAndView = new ModelAndView("profiles")
    mv.addObject("heading", "Profiles")
    commonModelObjectsService.populateCommonLocal(mv)
    mv.addObject("profiles", Await.result(mongoRepository.getAllUsers, tenSeconds))
    return mv
  }

  @RequestMapping(Array("/profiles/*"))
  @Timed(timingNotes = "") def view(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {

    def userByPath(path: String): Option[User] = {
      if (path.matches("^/profiles/.*$")) {
        val profilename = path.split("/")(2)
        Await.result(mongoRepository.getUserByProfilename(profilename), tenSeconds)
      } else {
        None
      }
    }

    userByPath(request.getPathInfo).map { user =>
      var mv = new ModelAndView("viewProfile")
      val loggedInUser = loggedInUserFilter.getLoggedInUser
      if (loggedInUser != null && loggedInUser.getId == user.getId) {
        mv = new ModelAndView("profile")
      }
      mv.addObject("heading", "User profile")
      commonModelObjectsService.populateCommonLocal(mv)
      mv.addObject("profileuser", user)
      mv.addObject("submitted", contentRetrievalService.getOwnedBy(user))
      mv.addObject("tagged", contentRetrievalService.getTaggedBy(user))
      mv

    }.getOrElse {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND)
      null
    }
  }

  @RequestMapping(Array("/archive"))
  @throws[IOException]
  def archive(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView
    urlStack.setUrlStack(request)
    commonModelObjectsService.populateCommonLocal(mv)
    mv.addObject("heading", "Archive")
    import scala.collection.JavaConverters._
    mv.addObject("archiveLinks", contentRetrievalService.getArchiveMonths.asJava)
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
    import scala.collection.JavaConverters._

    val publishers = Await.result(contentRetrievalService.getAllPublishers, tenSeconds).sortBy(_.title).map(p => frontendResourceMapper.createFrontendResourceFrom(p))
    mv.addObject("publishers", publishers.asJava)

    mv.setViewName("publishers")
    mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5, 1).asJava)
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
