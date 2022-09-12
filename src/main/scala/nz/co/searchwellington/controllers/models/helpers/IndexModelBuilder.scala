package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.frontend.Action
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.helpers.ArchiveLinksService
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap
import javax.servlet.http.HttpServletRequest
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Component class IndexModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService, rssUrlBuilder: RssUrlBuilder,
                                                val urlBuilder: UrlBuilder, archiveLinksService: ArchiveLinksService,
                                                commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder
  with CommonSizes with Pagination with ReasonableWaits with ArchiveMonths {

  private val MAX_OWNED_TO_SHOW_IN_RHS = 4

  def isValid(request: HttpServletRequest): Boolean = {
    val path = RequestPath.getPathFrom(request)
    path.matches("^/$") ||
      path.matches("^/json$") ||
      path.matches("^/rss$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]] = {
    for {
      latestNewsitems <- contentRetrievalService.getLatestNewsitems(MAX_NEWSITEMS, getPage(request), loggedInUser = loggedInUser)
    } yield {
      val mv = new ModelMap().
        addAttribute("heading", "Wellynews").
        addAttribute("description", "Wellington related newsitems").
        addAttribute("link", urlBuilder.fullyQualified(urlBuilder.getHomeUri)).
        addAttribute(MAIN_CONTENT, latestNewsitems.asJava)

      monthOfLastItem(latestNewsitems).map { month =>
        mv.addAttribute("main_content_moreurl", urlBuilder.getIntervalUrl(month))
      }

      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getBaseRssTitle, rssUrlBuilder.getBaseRssUrl)

      val submitActions = ListBuffer(
        Action("Submit website", urlBuilder.getSubmitWebsiteUrl),
        Action("Submit newsitem", urlBuilder.getSubmitNewsitemUrl),
        Action("Submit feed", urlBuilder.getSubmitFeedUrl)
      )
      if (loggedInUser.exists(_.isAdmin)) {
        submitActions :+ Action("Submit watchlist item", urlBuilder.getSubmitWatchlistUrl)
      }
      mv.addAttribute("submitActions", submitActions.asJava)

      Some(mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[ModelMap] = {
    def populateUserOwnedResources(loggedInUser: Option[User]): Future[ModelMap] = {
      val mv = new ModelMap()
      loggedInUser.map { loggedInUser =>
        for {
          owned <- contentRetrievalService.getOwnedBy(loggedInUser, Some(loggedInUser), MAX_OWNED_TO_SHOW_IN_RHS)
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
    val eventualArchiveStatistics = contentRetrievalService.getArchiveTypeCounts(loggedInUser)
    val eventualGeocodedNewsitems = contentRetrievalService.getGeocodedNewsitems(0, MAX_NUMBER_OF_GEOTAGGED_TO_SHOW, loggedInUser)
    val eventualUserOwnedResources = populateUserOwnedResources(loggedInUser)

    for {
      websites <- eventualWebsites
      archiveMonths <- eventualArchiveMonths
      archiveStatistics <- eventualArchiveStatistics
      geocodedNewsitems <- eventualGeocodedNewsitems
      userOwnedResources <- eventualUserOwnedResources
    } yield {
      new ModelMap()
        .addAllAttributes(secondaryJustin(websites._1))
        .addAllAttributes(populateGeocoded(geocodedNewsitems._1))
        .addAllAttributes(archiveLinksService.populateArchiveLinks(archiveMonths, archiveStatistics.toMap))
        .addAllAttributes(userOwnedResources)
    }
  }

  def getViewName(mv: ModelMap, loggedInUser: Option[User]): String = "index"
  
  private def populateGeocoded(geocoded: Seq[FrontendResource]): ModelMap = {
    if (geocoded.nonEmpty) {
      new ModelMap().addAttribute("geocoded", geocoded.asJava)
    } else {
      new ModelMap()
    }
  }

  private def secondaryJustin(websites: Seq[FrontendResource]): ModelMap = {
    new ModelMap().addAttribute("secondary_heading", "Just In")
      .addAttribute("secondary_description", "New additions.")
      .addAttribute("secondary_content", websites.asJava)
      .addAttribute("secondary_content_moreurl", "justin")
  }

}
