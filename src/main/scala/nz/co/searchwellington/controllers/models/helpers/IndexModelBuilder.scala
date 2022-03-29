package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.helpers.ArchiveLinksService
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.joda.time.{DateTime, Interval, YearMonth}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

@Component class IndexModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService, rssUrlBuilder: RssUrlBuilder,
                                                val urlBuilder: UrlBuilder, archiveLinksService: ArchiveLinksService,
                                                commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder
  with CommonSizes with Pagination with ReasonableWaits {

  private val MAX_OWNED_TO_SHOW_IN_RHS = 4

  def isValid(request: HttpServletRequest): Boolean = {
    val path = RequestPath.getPathFrom(request)
    path.matches("^/$") ||
      path.matches("^/json$") ||
      path.matches("^/rss$")
  }

  def getViewName(mv: ModelAndView, loggedInUser: Option[User]): String = "index"

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {

    def monthOfLastItem(newsitems: Seq[FrontendResource]): Option[Interval] = newsitems.lastOption.map { i =>
      new YearMonth(new DateTime(i.date)).toInterval()
    }

    for {
      latestNewsitems <- contentRetrievalService.getLatestNewsitems(MAX_NEWSITEMS, getPage(request), loggedInUser = loggedInUser)
    } yield {
      val mv = new ModelAndView().
        addObject("heading", "Wellynews").
        addObject("description", "Wellington related newsitems").
        addObject("link", urlBuilder.fullyQualified(urlBuilder.getHomeUri)).
        addObject(MAIN_CONTENT, latestNewsitems.asJava)

      monthOfLastItem(latestNewsitems).map { month =>
        mv.addObject("main_content_moreurl", urlBuilder.getIntervalUrl(month))
      }

      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getBaseRssTitle, rssUrlBuilder.getBaseRssUrl)
      Some(mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User]): Future[ModelMap] = {
    def populateUserOwnedResources(mv: ModelMap, l: Option[User]): Future[ModelMap] = {
      l.map { loggedInUser =>
        val eventualOwned = contentRetrievalService.getOwnedBy(loggedInUser, Some(loggedInUser), MAX_OWNED_TO_SHOW_IN_RHS)
        for {
          owned <- eventualOwned
        } yield {
          if (owned._2 > 0) {
            mv.addAttribute("owned", owned._1.asJava)
            if (owned._2 > MAX_OWNED_TO_SHOW_IN_RHS) {
              mv.addAttribute("owned_moreurl", urlBuilder.getProfileUrlFromProfileName(loggedInUser.getProfilename))
            }
          }
          mv
        }
      }.getOrElse {
        Future.successful(mv)
      }
    }

    val eventualWebsites = contentRetrievalService.getLatestWebsites(4, loggedInUser = loggedInUser)
    val eventualArchiveMonths = contentRetrievalService.getArchiveMonths(loggedInUser)
    val eventualArchiveStatistics = contentRetrievalService.getArchiveCounts(loggedInUser)
    val eventualGeocodedNewsitems = contentRetrievalService.getGeocodedNewsitems(0, MAX_NUMBER_OF_GEOTAGGED_TO_SHOW, loggedInUser)

    (for {
      websites <- eventualWebsites
      archiveMonths <- eventualArchiveMonths
      archiveStatistics <- eventualArchiveStatistics
      geocodedNewsitems <- eventualGeocodedNewsitems

    } yield {
      val mv = new ModelMap()
      populateSecondaryJustin(mv, websites._1)
      populateGeocoded(mv, geocodedNewsitems._1)
      archiveLinksService.populateArchiveLinks(mv, archiveMonths, archiveStatistics)
      mv
    }).flatMap { mv =>
      populateUserOwnedResources(mv, loggedInUser)  // TODO weird wiring
    }
  }

  private def populateGeocoded(mv: ModelMap, geocoded: Seq[FrontendResource]): Unit = {
    if (geocoded.nonEmpty) {
      mv.addAttribute("geocoded", geocoded.asJava)
    }
  }

  private def populateSecondaryJustin(mv: ModelMap, websites: Seq[FrontendResource]): Unit = {
    mv.addAttribute("secondary_heading", "Just In")
    mv.addAttribute("secondary_description", "New additions.")
    mv.addAttribute("secondary_content", websites.asJava)
    mv.addAttribute("secondary_content_moreurl", "justin")
  }

}
