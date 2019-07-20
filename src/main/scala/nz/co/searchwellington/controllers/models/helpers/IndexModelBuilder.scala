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
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.collection.JavaConverters._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

@Component class IndexModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService, rssUrlBuilder: RssUrlBuilder,
                                                loggedInUserFilter: LoggedInUserFilter, urlBuilder: UrlBuilder, archiveLinksService: ArchiveLinksService,
                                                commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder with CommonSizes with Pagination with ReasonableWaits {

  private val log = Logger.getLogger(classOf[IndexModelBuilder])

  private val MAX_OWNED_TO_SHOW_IN_RHS = 4

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/$") || request.getPathInfo.matches("^/json$")
  }

  def getViewName(mv: ModelAndView): String = "index"

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {

    def monthOfLastItem(newsitems: Seq[FrontendResource]): Option[Date] = newsitems.lastOption.map(i => i.getDate)

    Await.result(if (isValid(request)) {
      for {
        latestNewsitems <- contentRetrievalService.getLatestNewsitems(MAX_NEWSITEMS * 3, getPage(request), loggedInUser = Option(loggedInUserFilter.getLoggedInUser))
      } yield {
        val mv = new ModelAndView
        log.info("Main content newitems: " + latestNewsitems.size)
        mv.addObject(MAIN_CONTENT, latestNewsitems.asJava)
        monthOfLastItem(latestNewsitems).map { d =>
          mv.addObject("main_content_moreurl", urlBuilder.getArchiveLinkUrl(d))
        }

        commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getBaseRssTitle, rssUrlBuilder.getBaseRssUrl)
        Some(mv)
      }

    } else {
      Future.successful(None)
    }, TenSeconds)
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView): Unit = {

    val loggedInUser = Option(loggedInUserFilter.getLoggedInUser)

    def populateUserOwnedResources(mv: ModelAndView, loggedInUser: User) {
      if (loggedInUser != null) {
        val ownedCount: Int = contentRetrievalService.getOwnedByCount(loggedInUser)
        if (ownedCount > 0) {
          mv.addObject("owned", contentRetrievalService.getOwnedBy(loggedInUser, Some(loggedInUser)))
          if (ownedCount > MAX_OWNED_TO_SHOW_IN_RHS) {
            mv.addObject("owned_moreurl", urlBuilder.getProfileUrlFromProfileName(loggedInUser.getProfilename))
          }
        }
      }
    }

    val eventualWebsites = contentRetrievalService.getLatestWebsites(4, loggedInUser = loggedInUser)
    val eventualArchiveMonths = contentRetrievalService.getArchiveMonths(loggedInUser)
    val eventualArchiveStatistics = contentRetrievalService.getArchiveCounts(loggedInUser)
    val eventualGeocoded = contentRetrievalService.getGeocodedNewsitems(0, MAX_NUMBER_OF_GEOTAGGED_TO_SHOW, loggedInUser)

    val eventualPopulated = for {
      websites <- eventualWebsites
      archiveMonths <- eventualArchiveMonths
      archiveStatistics <- eventualArchiveStatistics
      geocoded <- eventualGeocoded

    } yield {
      populateSecondaryJustin(mv, websites)
      populateGeocoded(mv, geocoded)
      // populateUserOwnedResources(mv, loggedInUserFilter.getLoggedInUser)
      archiveLinksService.populateArchiveLinks(mv, archiveMonths, archiveStatistics)
    }

    Await.result(eventualPopulated, TenSeconds)
  }

  private def populateGeocoded(mv: ModelAndView, geocoded: Seq[FrontendResource]) {
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
