package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.filters.{LocationParameterFilter, RequestPath}
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.tagging.RelatedTagsService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.geo.model.Place

import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

@Component class GeotaggedModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                    val urlBuilder: UrlBuilder,
                                                    rssUrlBuilder: RssUrlBuilder,
                                                    relatedTagsService: RelatedTagsService,
                                                    commonAttributesModelBuilder: CommonAttributesModelBuilder)
  extends ModelBuilder with CommonSizes with Pagination with ReasonableWaits {

  private val log = Logger.getLogger(classOf[GeotaggedModelBuilder])

  private val HOW_FAR_IS_CLOSE_IN_KILOMETERS = 1.0

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches("^/geotagged(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {
    val mv = new ModelAndView
    mv.addObject("description", "Geotagged newsitems")
    mv.addObject("link", urlBuilder.fullyQualified(urlBuilder.getGeotaggedUrl))

    val userSuppliedPlace = request.getAttribute(LocationParameterFilter.LOCATION).asInstanceOf[Place]
    val hasUserSuppliedALocation = userSuppliedPlace != null && userSuppliedPlace.getLatLong != null

    val page = getPage(request)
    mv.addObject("page", page)
    val startIndex = getStartIndex(page, MAX_NEWSITEMS)

    if (hasUserSuppliedALocation) { // TODO split into seperate model
      val radius = getLocationSearchRadius(request)
      val latLong = userSuppliedPlace.getLatLong

      val eventualRelatedTagsForLocation = relatedTagsService.getRelatedTagsForLocation(userSuppliedPlace, radius, loggedInUser)
      val eventualPublishersForLocation = relatedTagsService.getRelatedPublishersForLocation(userSuppliedPlace, radius, loggedInUser)
      val eventualNewsitemsNearCount = contentRetrievalService.getNewsitemsNearCount(latLong, radius, loggedInUser = loggedInUser)
      val eventualNewsitemsNear = contentRetrievalService.getNewsitemsNear(latLong, radius, startIndex, MAX_NEWSITEMS, loggedInUser)
      for {
        relatedTagLinks <- eventualRelatedTagsForLocation
        relatedPublisherLinks <- eventualPublishersForLocation
        totalNearbyCount <- eventualNewsitemsNearCount
        newsitemsNear <- eventualNewsitemsNear

      } yield {
        if (startIndex > totalNearbyCount) {
          None
        }

        populatePagination(mv, startIndex, totalNearbyCount, MAX_NEWSITEMS, paginationLinks)

        mv.addObject("location", userSuppliedPlace)
        mv.addObject("radius", radius)
        mv.addObject(MAIN_CONTENT, newsitemsNear.asJava)

        if (relatedTagLinks.nonEmpty) {
          log.info("Found geo related tags: " + relatedTagLinks)
          mv.addObject("related_tags", relatedTagLinks.asJava)
        }
        if (relatedPublisherLinks.nonEmpty) {
          mv.addObject("related_publishers", relatedPublisherLinks.asJava)
        }

        mv.addObject("heading", rssUrlBuilder.getRssTitleForPlace(userSuppliedPlace, radius))
        setRssUrlForLocation(mv, userSuppliedPlace, radius)
        Some(mv)
      }

    } else {
      for {
        // TODO combine queries?
        totalGeotaggedCount <- contentRetrievalService.getGeocodedNewitemsCount(loggedInUser)
        geocodedNewsitems <- contentRetrievalService.getGeocodedNewsitems(startIndex, MAX_NEWSITEMS, loggedInUser)
      } yield {
        if (startIndex > totalGeotaggedCount) {
          None
        }
        populatePagination(mv, startIndex, totalGeotaggedCount, MAX_NEWSITEMS, paginationLinks)

        mv.addObject("heading", "Geotagged newsitems")
        mv.addObject(MAIN_CONTENT, geocodedNewsitems.asJava)
        commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForGeotagged, rssUrlBuilder.getRssUrlForGeotagged)
        Some(mv)
      }
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = {
    withLatestNewsitems(mv, loggedInUser)
  }

  def getViewName(mv: ModelAndView, loggedInUser: Option[User]): String = "geocoded"

  private def setRssUrlForLocation(mv: ModelAndView, place: Place, radius: Double) {
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
