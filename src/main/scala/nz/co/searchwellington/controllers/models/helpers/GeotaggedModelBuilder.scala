package nz.co.searchwellington.controllers.models.helpers

import java.util.List
import javax.servlet.http.HttpServletRequest

import com.google.common.base.Strings
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

  def populateContentModel(request: HttpServletRequest): ModelAndView = {
    if (isValid(request)) {
      log.debug("Building geotagged page model")
      val mv: ModelAndView = new ModelAndView
      mv.addObject("description", "Geotagged newsitems")
      mv.addObject("link", urlBuilder.getGeotaggedUrl)
      val userSuppliedPlace: Place = request.getAttribute(LocationParameterFilter.LOCATION).asInstanceOf[Place]
      val hasUserSuppliedALocation: Boolean = userSuppliedPlace != null && userSuppliedPlace.getLatLong != null
      if (hasUserSuppliedALocation) {
        val latLong: LatLong = userSuppliedPlace.getLatLong
        log.debug("Location is set to: " + userSuppliedPlace.getLatLong)
        val page: Int = commonAttributesModelBuilder.getPage(request)
        mv.addObject("page", page)
        val startIndex: Int = commonAttributesModelBuilder.getStartIndex(page)
        val radius: Double = getLocationSearchRadius(request)
        mv.addObject("radius", radius)
        val totalNearbyCount: Long = contentRetrievalService.getNewsitemsNearCount(latLong, radius)
        if (startIndex > totalNearbyCount) {
          return null
        }
        commonAttributesModelBuilder.populatePagination(mv, startIndex, totalNearbyCount)
        mv.addObject("location", userSuppliedPlace)
        log.debug("Populating main content with newsitems near: " + latLong + " (radius: " + radius + ")")
        mv.addObject("main_content", contentRetrievalService.getNewsitemsNear(latLong, radius, startIndex, CommonAttributesModelBuilder.MAX_NEWSITEMS))
        mv.addObject("related_distances", contentRetrievalService.getNewsitemsNearDistanceFacet(latLong))
        if (request.getAttribute(LocationParameterFilter.LOCATION) == null) {
          mv.addObject("geotagged_tags", contentRetrievalService.getGeotaggedTags)
        }
        else {
          val relatedTagLinks: List[TagContentCount] = relatedTagsService.getRelatedTagsForLocation(userSuppliedPlace, radius, REFINEMENTS_TO_SHOW)
          if (!relatedTagLinks.isEmpty) {
            mv.addObject("related_tags", relatedTagLinks)
          }
          val relatedPublisherLinks: List[PublisherContentCount] = relatedTagsService.getRelatedPublishersForLocation(userSuppliedPlace, radius)
          if (!relatedPublisherLinks.isEmpty) {
            mv.addObject("related_publishers", relatedPublisherLinks)
          }
        }
        if (!Strings.isNullOrEmpty(userSuppliedPlace.getAddress)) {
          mv.addObject("heading", rssUrlBuilder.getRssTitleForPlace(userSuppliedPlace, radius))
        }
        setRssUrlForLocation(mv, userSuppliedPlace, radius)
        return mv
      }
      val page: Int = commonAttributesModelBuilder.getPage(request)
      mv.addObject("page", page)
      val startIndex: Int = commonAttributesModelBuilder.getStartIndex(page)
      val totalGeotaggedCount: Long = contentRetrievalService.getGeotaggedCount
      if (startIndex > totalGeotaggedCount) {
        return null
      }
      mv.addObject("heading", "Geotagged newsitems")
      mv.addObject("main_content", contentRetrievalService.getGeocoded(startIndex, CommonAttributesModelBuilder.MAX_NEWSITEMS))
      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForGeotagged, rssUrlBuilder.getRssUrlForGeotagged)
      commonAttributesModelBuilder.populatePagination(mv, startIndex, totalGeotaggedCount)
      return mv
    }
    return null
  }

  private def getLocationSearchRadius(request: HttpServletRequest): Double = {
    var radius: Double = HOW_FAR_IS_CLOSE_IN_KILOMETERS
    if (request.getAttribute(LocationParameterFilter.RADIUS) != null) {
      radius = request.getAttribute(LocationParameterFilter.RADIUS).asInstanceOf[Double]
    }
    return radius
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5))
  }

  def getViewName(mv: ModelAndView): String = {
    return "geocoded"
  }

  private def setRssUrlForLocation(mv: ModelAndView, place: Place, radius: Double) {
    val rssUrlForPlace: String = rssUrlBuilder.getRssUrlForPlace(place, radius)
    if (rssUrlForPlace == null) {
      return
    }
    commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForPlace(place, radius), rssUrlForPlace)
  }

}