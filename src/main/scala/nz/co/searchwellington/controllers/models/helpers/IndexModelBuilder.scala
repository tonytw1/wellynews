package nz.co.searchwellington.controllers.models.helpers

import java.util.Date

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
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
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

@Component class IndexModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService, rssUrlBuilder: RssUrlBuilder,
                                                loggedInUserFilter: LoggedInUserFilter, urlBuilder: UrlBuilder, archiveLinksService: ArchiveLinksService,
                                                commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder with CommonSizes with Pagination with ReasonableWaits {

  private val MAX_OWNED_TO_SHOW_IN_RHS = 4
  private val NUMBER_OF_COMMENTED_TO_SHOW = 2

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/$") || request.getPathInfo.matches("^/json$")
  }

  def getViewName(mv: ModelAndView): String = {
    "index"
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {

    def monthOfLastItem(newsitems: Seq[FrontendResource]): Option[Date] = newsitems.lastOption.map(i => i.getDate)

    if (isValid(request)) {
      val eventualNewsitems = contentRetrievalService.getLatestNewsitems(MAX_NEWSITEMS, getPage(request))

      val eventualModelAndView = for {
        latestNewsitems <- eventualNewsitems
      } yield {
        val mv = new ModelAndView
        mv.addObject(MAIN_CONTENT, latestNewsitems.asJava)
        monthOfLastItem(latestNewsitems).map { d =>
          mv.addObject("main_content_moreurl", urlBuilder.getArchiveLinkUrl(d))
        }

        commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getBaseRssTitle, rssUrlBuilder.getBaseRssUrl)
        mv
      }
      Some(Await.result(eventualModelAndView, TenSeconds))

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

    val eventualWebsites = contentRetrievalService.getLatestWebsites(4)
    val eventualArchiveStatistics = contentRetrievalService.getArchiveCounts

    val eventualPopulated = for {
      websites <- eventualWebsites
      archiveMonths <- contentRetrievalService.getArchiveMonths
      archiveStatistics <- eventualArchiveStatistics


    } yield {
      populateSecondaryJustin(mv, websites)
      // populateGeocoded(mv)
      // populateUserOwnedResources(mv, loggedInUserFilter.getLoggedInUser)
      //archiveLinksService.populateArchiveLinks(mv, archiveMonths, archiveStatistics)
    }

    Await.result(eventualPopulated, TenSeconds)
  }

  private def populateGeocoded(mv: ModelAndView) {
    val geocoded = contentRetrievalService.getGeocodedNewsitems(0, MAX_NUMBER_OF_GEOTAGGED_TO_SHOW).toList
    if (geocoded.nonEmpty) {
      mv.addObject("geocoded", geocoded.asJava)
    }
  }

  private def populateSecondaryJustin(mv: ModelAndView, websites: Seq[FrontendResource]) {
    mv.addObject("secondary_heading", "Just In")
    mv.addObject("secondary_description", "New additions.")
    import scala.collection.JavaConverters._
    mv.addObject("secondary_content", websites.asJava)
    mv.addObject("secondary_content_moreurl", "justin")
  }

}
