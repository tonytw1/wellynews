package nz.co.searchwellington.controllers.models.helpers

import java.util.Date
import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RssUrlBuilder}
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.helpers.ArchiveLinksService
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

@Component class IndexModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService, rssUrlBuilder: RssUrlBuilder, loggedInUserFilter: LoggedInUserFilter, urlBuilder: UrlBuilder, archiveLinksService: ArchiveLinksService, commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder with CommonSizes {

  private val MAX_OWNED_TO_SHOW_IN_RHS = 4
  private val NUMBER_OF_COMMENTED_TO_SHOW = 2

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/$") || request.getPathInfo.matches("^/json$")
  }

  def getViewName(mv: ModelAndView): String = {
    "index"
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {

    def monthOfLastItem(newitems: Seq[FrontendResource]): Option[Date] = {
      newitems.headOption.map( i => i.getDate)
    }

    if (isValid(request)) {  // TODO really? won't the dispatcher alway have decided this?
      val page = if (request.getParameter("page") != null) {  // TODO duplication
        Integer.parseInt(request.getParameter("page"))
      } else {
        1
      }

      val mv = new ModelAndView
      val latestNewsitems = contentRetrievalService.getLatestNewsitems(MAX_NEWSITEMS, page).toList
      mv.addObject(MAIN_CONTENT, latestNewsitems)

      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getBaseRssTitle, rssUrlBuilder.getBaseRssUrl)
      if (latestNewsitems != null) {
        monthOfLastItem(latestNewsitems).map { d =>
          mv.addObject("main_content_moreurl", urlBuilder.getArchiveLinkUrl(d))
        }
      }
      Some(mv)

    } else {
      None
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {

    val COMMENTED_NEWSITEMS = "commented_newsitems"

    def populateCommentedNewsitems(mv: ModelAndView) {
      val recentCommentedNewsitems = contentRetrievalService.getCommentedNewsitems(NUMBER_OF_COMMENTED_TO_SHOW + 1, 0).toList
      if (recentCommentedNewsitems.size <= NUMBER_OF_COMMENTED_TO_SHOW) {
        mv.addObject(COMMENTED_NEWSITEMS, recentCommentedNewsitems)
      }
      else {
        mv.addObject(COMMENTED_NEWSITEMS, recentCommentedNewsitems.subList(0, NUMBER_OF_COMMENTED_TO_SHOW))
        mv.addObject("commented_newsitems_moreurl", "comment")
      }
    }

    def populateUserOwnedResources(mv: ModelAndView, loggedInUser: User) {
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

    populateCommentedNewsitems(mv)
    populateSecondaryJustin(mv)
    populateGeocoded(mv)
    populateFeatured(mv)
    populateUserOwnedResources(mv, loggedInUserFilter.getLoggedInUser)
    archiveLinksService.populateArchiveLinks(mv, contentRetrievalService.getArchiveMonths)
  }

  private def populateFeatured(mv: ModelAndView) {
    mv.addObject("featured", contentRetrievalService.getFeaturedSites)
  }

  private def populateGeocoded(mv: ModelAndView) {
    val geocoded = contentRetrievalService.getGeocoded(0, MAX_NUMBER_OF_GEOTAGGED_TO_SHOW).toList
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
