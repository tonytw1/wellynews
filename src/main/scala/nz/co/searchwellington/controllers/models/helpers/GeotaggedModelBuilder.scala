package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.filters.{LocationParameterFilter, RequestPath}
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.tagging.RelatedTagsService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap
import uk.co.eelpieconsulting.common.geo.model.Place

import javax.servlet.http.HttpServletRequest
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Component class GeotaggedModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                    val urlBuilder: UrlBuilder,
                                                    rssUrlBuilder: RssUrlBuilder,
                                                    relatedTagsService: RelatedTagsService,
                                                    commonAttributesModelBuilder: CommonAttributesModelBuilder)
  extends ModelBuilder with CommonSizes with Pagination with ReasonableWaits {

  private val HOW_FAR_IS_CLOSE_IN_KILOMETERS = 1.0

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches("^/geotagged(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]] = {
    def processModel(loggedInUser: Option[User], mv: ModelMap, startIndex: Int): Future[Option[ModelMap]] = {
      for {
        geocodedNewsitems <- contentRetrievalService.getGeocodedNewsitems(startIndex, MAX_NEWSITEMS, loggedInUser)
      } yield {
        if (startIndex < geocodedNewsitems._2) {
          populatePagination(mv, startIndex, geocodedNewsitems._2, MAX_NEWSITEMS, paginationLinks)
          mv.addAttribute("heading", "Geotagged newsitems")
          mv.addAttribute(MAIN_CONTENT, geocodedNewsitems._1.asJava)
          commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForGeotagged, rssUrlBuilder.getRssUrlForGeotagged)
          Some(mv)
        } else {
          None
        }
      }
    }

    def processLocationSuppliedModel(request: HttpServletRequest, loggedInUser: Option[User], mv: ModelMap, userSuppliedPlace: Place, startIndex: Int): Future[Option[ModelMap]] = {
      val radius = getLocationSearchRadius(request)
      val latLong = userSuppliedPlace.getLatLong

      val eventualRelatedTagsForLocation = relatedTagsService.getRelatedTagsForLocation(userSuppliedPlace, radius, loggedInUser)
      val eventualPublishersForLocation = relatedTagsService.getRelatedPublishersForLocation(userSuppliedPlace, radius, loggedInUser)
      val eventualNewsitemsNear = contentRetrievalService.getNewsitemsNear(latLong, radius, startIndex, MAX_NEWSITEMS, loggedInUser)
      for {
        relatedTagLinks <- eventualRelatedTagsForLocation
        relatedPublisherLinks <- eventualPublishersForLocation
        newsitemsNear <- eventualNewsitemsNear

      } yield {
        if (startIndex < newsitemsNear._2) {
          populatePagination(mv, startIndex, newsitemsNear._2, MAX_NEWSITEMS, paginationLinks)

          mv.addAttribute("location", userSuppliedPlace)
          mv.addAttribute("radius", radius)
          mv.addAttribute(MAIN_CONTENT, newsitemsNear._1.asJava)

          if (relatedTagLinks.nonEmpty) {
            mv.addAttribute("related_tags", relatedTagLinks.asJava)
          }
          if (relatedPublisherLinks.nonEmpty) {
            mv.addAttribute("related_publishers", relatedPublisherLinks.asJava)
          }

          mv.addAttribute("heading", rssUrlBuilder.getRssTitleForPlace(userSuppliedPlace, radius))
          setRssUrlForLocation(mv, userSuppliedPlace, radius)
          Some(mv)

        } else {
          None
        }
      }
    }

    val mv = new ModelMap().
      addAttribute("description", "Geotagged newsitems").
      addAttribute("link", urlBuilder.fullyQualified(urlBuilder.getGeotaggedUrl))

    val userSuppliedPlace = request.getAttribute(LocationParameterFilter.LOCATION).asInstanceOf[Place]
    val hasUserSuppliedALocation = userSuppliedPlace != null && userSuppliedPlace.getLatLong != null

    val page = getPage(request)
    mv.addAttribute("page", page)
    val startIndex = getStartIndex(page, MAX_NEWSITEMS)

    if (hasUserSuppliedALocation) {
      processLocationSuppliedModel(request, loggedInUser, mv, userSuppliedPlace, startIndex)   // TODO split into separate model
    } else {
      processModel(loggedInUser, mv, startIndex)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[ModelMap] = {
    latestNewsitems(loggedInUser)
  }

  def getViewName(mv: ModelMap, loggedInUser: Option[User]): String = "geocoded"

  private def setRssUrlForLocation(mv: ModelMap, place: Place, radius: Double): Unit = {
    commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForPlace(place, radius), rssUrlBuilder.getRssUrlForPlace(place, radius))
  }

  private def getLocationSearchRadius(request: HttpServletRequest): Double = {
    if (request.getAttribute(LocationParameterFilter.RADIUS) != null) {
      request.getAttribute(LocationParameterFilter.RADIUS).asInstanceOf[Double]
    } else {
      HOW_FAR_IS_CLOSE_IN_KILOMETERS
    }
  }

  private def paginationLinks(page: Int): String = {
    urlBuilder.getGeotaggedUrl + "?page=" + page  // TODO push to URL builder
  }

}
