package nz.co.searchwellington.controllers.models.helpers

import java.util.Date
import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RssUrlBuilder}
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

@Component class IndexModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService, rssUrlBuilder: RssUrlBuilder, loggedInUserFilter: LoggedInUserFilter, urlBuilder: UrlBuilder, archiveLinksService: ArchiveLinksService, commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder {

  private val MAX_OWNED_TO_SHOW_IN_RHS: Int = 4
  private val NUMBER_OF_COMMENTED_TO_SHOW: Int = 2

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/$") || request.getPathInfo.matches("^/json$")
  }

  def getViewName(mv: ModelAndView) = {
    "index"
  }

  def populateContentModel(request: HttpServletRequest): ModelAndView = {
    if (!isValid(request)) {  // TODO really? won't the dispatcher alway have decided this?
      return null;
    }

    val mv: ModelAndView = new ModelAndView
    val latestNewsitems: List[FrontendResource] = contentRetrievalService.getLatestNewsitems(CommonAttributesModelBuilder.MAX_NEWSITEMS).toList
    mv.addObject("main_content", latestNewsitems.asJava)
    commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getBaseRssTitle, rssUrlBuilder.getBaseRssUrl)
    if (latestNewsitems != null && !latestNewsitems.isEmpty) {
        mv.addObject("main_content_moreurl", urlBuilder.getArchiveLinkUrl(monthOfLastItem(latestNewsitems)))
    }
    return mv
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    populateCommentedNewsitems(mv)
    populateSecondaryJustin(mv)
    populateGeocoded(mv)
    populateFeatured(mv)
    populateUserOwnedResources(mv, loggedInUserFilter.getLoggedInUser)
    archiveLinksService.populateArchiveLinks(mv, contentRetrievalService.getArchiveMonths)
  }

  private def monthOfLastItem(latestNewsitems: List[FrontendResource]): Date = {
    if (latestNewsitems.size > 0) {
      val lastNewsitem: FrontendResource = latestNewsitems.get(latestNewsitems.size - 1)
      if (lastNewsitem.getDate != null) {
        return lastNewsitem.getDate
      }
    }
    return null
  }

  private def populateUserOwnedResources(mv: ModelAndView, loggedInUser: User) {
    if (loggedInUser != null) {
      val ownedCount: Int = contentRetrievalService.getOwnedByCount(loggedInUser)
      if (ownedCount > 0) {
        mv.addObject("owned", contentRetrievalService.getOwnedBy(loggedInUser))
        if (ownedCount > MAX_OWNED_TO_SHOW_IN_RHS) {
          mv.addObject("owned_moreurl", urlBuilder.getProfileUrlFromProfileName(loggedInUser.getProfilename))
        }
      }
    }
  }

  private def populateFeatured(mv: ModelAndView) {
    mv.addObject("featured", contentRetrievalService.getFeaturedSites)
  }

  private def populateCommentedNewsitems(mv: ModelAndView) {
    val recentCommentedNewsitems: List[FrontendResource] = contentRetrievalService.getCommentedNewsitems(NUMBER_OF_COMMENTED_TO_SHOW + 1, 0).toList
    if (recentCommentedNewsitems.size <= NUMBER_OF_COMMENTED_TO_SHOW) {
      mv.addObject("commented_newsitems", recentCommentedNewsitems)
    }
    else {
      mv.addObject("commented_newsitems", recentCommentedNewsitems.subList(0, NUMBER_OF_COMMENTED_TO_SHOW))
      mv.addObject("commented_newsitems_moreurl", "comment")
    }
  }

  private def populateGeocoded(mv: ModelAndView) {
    val geocoded: List[FrontendResource] = contentRetrievalService.getGeocoded(0, CommonAttributesModelBuilder.MAX_NUMBER_OF_GEOTAGGED_TO_SHOW).toList
    if (!geocoded.isEmpty) {
      mv.addObject("geocoded", geocoded.asJava)
    }
  }

  private def populateSecondaryJustin(mv: ModelAndView) {
    mv.addObject("secondary_heading", "Just In")
    mv.addObject("secondary_description", "New additions.")
    mv.addObject("secondary_content", contentRetrievalService.getLatestWebsites(4))
    mv.addObject("secondary_content_moreurl", "justin")
  }

}