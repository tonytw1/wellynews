package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.controllers.{RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.filters.LocationParameterFilter
import nz.co.searchwellington.model.{PublisherContentCount, TagContentCount}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.geo.model.{LatLong, Place}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

@Component class GeotaggedModelBuilder @Autowired() (contentRetrievalService: ContentRetrievalService,
                                                     urlBuilder: UrlBuilder,
                                                     rssUrlBuilder: RssUrlBuilder,
                                                     relatedTagsService: RelatedTagsService,
                                                     commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder {

  private val log: Logger = Logger.getLogger(classOf[GeotaggedModelBuilder])

  private val REFINEMENTS_TO_SHOW: Int = 8
  private val HOW_FAR_IS_CLOSE_IN_KILOMETERS: Double = 1.0

  def isValid(request: HttpServletRequest): Boolean = {
    return request.getPathInfo.matches("^/geotagged(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {
    if (isValid(request)) {
      val mv = new ModelAndView
      mv.addObject("description", "Geotagged newsitems")
      mv.addObject("link", urlBuilder.getGeotaggedUrl)

      val userSuppliedPlace: Place = request.getAttribute(LocationParameterFilter.LOCATION).asInstanceOf[Place]
      val hasUserSuppliedALocation: Boolean = userSuppliedPlace != null && userSuppliedPlace.getLatLong != null

      val page = commonAttributesModelBuilder.getPage(request)
      mv.addObject("page", page)
      val startIndex = commonAttributesModelBuilder.getStartIndex(page)

      if (hasUserSuppliedALocation) {
        val radius = getLocationSearchRadius(request)
        val latLong = userSuppliedPlace.getLatLong
        val totalNearbyCount = contentRetrievalService.getNewsitemsNearCount(latLong, radius)
        if (startIndex > totalNearbyCount) {
          None
        }

        commonAttributesModelBuilder.populatePagination(mv, startIndex, totalNearbyCount)
        mv.addObject("location", userSuppliedPlace)
        mv.addObject("radius", radius)
        mv.addObject("main_content", contentRetrievalService.getNewsitemsNear(latLong, radius, startIndex, CommonAttributesModelBuilder.MAX_NEWSITEMS))
        mv.addObject("related_distances", contentRetrievalService.getNewsitemsNearDistanceFacet(latLong))

        if (request.getAttribute(LocationParameterFilter.LOCATION) == null) {
          mv.addObject("geotagged_tags", contentRetrievalService.getGeotaggedTags)
        }
        else {
          val relatedTagLinks: List[TagContentCount] = relatedTagsService.getRelatedTagsForLocation(userSuppliedPlace, radius, REFINEMENTS_TO_SHOW).toList
          if (!relatedTagLinks.isEmpty) {
            mv.addObject("related_tags", relatedTagLinks.asJava)
          }
          val relatedPublisherLinks: List[PublisherContentCount] = relatedTagsService.getRelatedPublishersForLocation(userSuppliedPlace, radius).toList
          if (!relatedPublisherLinks.isEmpty) {
            mv.addObject("related_publishers", relatedPublisherLinks.asJava)
          }
        }

        mv.addObject("heading", rssUrlBuilder.getRssTitleForPlace(userSuppliedPlace, radius))
        setRssUrlForLocation(mv, userSuppliedPlace, radius)
        Some(mv)
      }

      val totalGeotaggedCount = contentRetrievalService.getGeotaggedCount
      if (startIndex > totalGeotaggedCount) {
        None
      }
      commonAttributesModelBuilder.populatePagination(mv, startIndex, totalGeotaggedCount)

      mv.addObject("heading", "Geotagged newsitems")
      mv.addObject("main_content", contentRetrievalService.getGeocoded(startIndex, CommonAttributesModelBuilder.MAX_NEWSITEMS))
      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForGeotagged, rssUrlBuilder.getRssUrlForGeotagged)
      Some(mv)
    }

    None
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5, 1))
  }

  def getViewName(mv: ModelAndView): String = {
    return "geocoded"
  }

  private def setRssUrlForLocation(mv: ModelAndView, place: Place, radius: Double) {
    commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForPlace(place, radius), rssUrlBuilder.getRssUrlForPlace(place, radius))
  }

  private def getLocationSearchRadius(request: HttpServletRequest): Double = {
    var radius = HOW_FAR_IS_CLOSE_IN_KILOMETERS
    if (request.getAttribute(LocationParameterFilter.RADIUS) != null) {
      radius = request.getAttribute(LocationParameterFilter.RADIUS).asInstanceOf[Double]
    }
    return radius
  }

}