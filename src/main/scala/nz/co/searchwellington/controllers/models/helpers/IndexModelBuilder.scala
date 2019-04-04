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

import scala.collection.JavaConverters._

@Component class IndexModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService, rssUrlBuilder: RssUrlBuilder,
                                                loggedInUserFilter: LoggedInUserFilter, urlBuilder: UrlBuilder, archiveLinksService: ArchiveLinksService,
                                                commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder with CommonSizes with Pagination {

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
      newitems.headOption.map(i => i.getDate)
    }

    if (isValid(request)) {  val mv = new ModelAndView
      val latestNewsitems = contentRetrievalService.getLatestNewsitems(MAX_NEWSITEMS, getPage(request))
      mv.addObject(MAIN_CONTENT, latestNewsitems.asJava)
      monthOfLastItem(latestNewsitems).map { d =>
        mv.addObject("main_content_moreurl", urlBuilder.getArchiveLinkUrl(d))
      }

      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getBaseRssTitle, rssUrlBuilder.getBaseRssUrl)
      Some(mv)

    } else {
      None
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {

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

    populateSecondaryJustin(mv)
    populateGeocoded(mv)
    populateFeatured(mv)
    populateUserOwnedResources(mv, loggedInUserFilter.getLoggedInUser)
    archiveLinksService.populateArchiveLinks(mv, contentRetrievalService.getArchiveMonths)
  }

  private def populateFeatured(mv: ModelAndView) {
    //mv.addObject("featured", contentRetrievalService.getFeaturedSites)
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
    import scala.collection.JavaConverters._
    mv.addObject("secondary_content", contentRetrievalService.getLatestWebsites(4).asJava)
    mv.addObject("secondary_content_moreurl", "justin")
  }

}
