package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.filters.attributesetters.LocationParameterFilter
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
  extends ModelBuilder with CommonSizes with ReasonableWaits {

  private val HOW_FAR_IS_CLOSE_IN_KILOMETERS = 1.0

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches("^/geotagged(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]] = {
    def processModel(loggedInUser: Option[User], mv: ModelMap): Future[Option[ModelMap]] = {
      for {
        geocodedNewsitems <- contentRetrievalService.getGeocodedNewsitems(MAX_NEWSITEMS, loggedInUser)
      } yield {
        mv.addAttribute("heading", "Geotagged newsitems")
        mv.addAttribute(MAIN_CONTENT, geocodedNewsitems._1.asJava)
        commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForGeotagged, rssUrlBuilder.getRssUrlForGeotagged)
        Some(mv)
      }
    }

    def processLocationSuppliedModel(request: HttpServletRequest, loggedInUser: Option[User], mv: ModelMap, userSuppliedPlace: Place): Future[Option[ModelMap]] = {
      val radius = getLocationSearchRadius(request)
      val latLong = userSuppliedPlace.getLatLong

      val eventualRelatedTagsForLocation = relatedTagsService.getRelatedTagsForLocation(userSuppliedPlace, radius, loggedInUser)
      val eventualPublishersForLocation = relatedTagsService.getRelatedPublishersForLocation(userSuppliedPlace, radius, loggedInUser)
      val eventualNewsitemsNear = contentRetrievalService.getNewsitemsNear(latLong, radius, MAX_NEWSITEMS, loggedInUser)
      for {
        relatedTagLinks <- eventualRelatedTagsForLocation
        relatedPublisherLinks <- eventualPublishersForLocation
        newsitemsNear <- eventualNewsitemsNear

      } yield {
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
      }
    }

    val mv = new ModelMap().
      addAttribute("description", "Geotagged newsitems").
      addAttribute("link", urlBuilder.fullyQualified(urlBuilder.getGeotaggedUrl))

    val userSuppliedPlace = request.getAttribute(LocationParameterFilter.LOCATION).asInstanceOf[Place]
    val hasUserSuppliedALocation = userSuppliedPlace != null && userSuppliedPlace.getLatLong != null

    if (hasUserSuppliedALocation) {
      processLocationSuppliedModel(request, loggedInUser, mv, userSuppliedPlace)   // TODO split into separate model
    } else {
      processModel(loggedInUser, mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[ModelMap] = {
    latestNewsitems(loggedInUser)
  }

  def getViewName(mv: ModelMap, loggedInUser: Option[User]): String = "geotagged"

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

}
