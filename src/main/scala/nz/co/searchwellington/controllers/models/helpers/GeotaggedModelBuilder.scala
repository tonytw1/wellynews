package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.filters.LocationParameterFilter
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.geo.model.Place

import scala.collection.JavaConverters._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

@Component class GeotaggedModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService,
                                                    urlBuilder: UrlBuilder,
                                                    rssUrlBuilder: RssUrlBuilder,
                                                    relatedTagsService: RelatedTagsService,
                                                    commonAttributesModelBuilder: CommonAttributesModelBuilder,
                                                    loggedInUserFilter: LoggedInUserFilter)
  extends ModelBuilder with CommonSizes with Pagination with ReasonableWaits {

  private val log = Logger.getLogger(classOf[GeotaggedModelBuilder])

  private val HOW_FAR_IS_CLOSE_IN_KILOMETERS = 1.0

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/geotagged(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest): Future[Option[ModelAndView]] = {
    if (isValid(request)) {
      val mv = new ModelAndView
      mv.addObject("description", "Geotagged newsitems")
      mv.addObject("link", urlBuilder.getGeotaggedUrl)

      val userSuppliedPlace = request.getAttribute(LocationParameterFilter.LOCATION).asInstanceOf[Place]
      val hasUserSuppliedALocation = userSuppliedPlace != null && userSuppliedPlace.getLatLong != null

      val page = getPage(request)
      mv.addObject("page", page)
      val startIndex = getStartIndex(page, MAX_NEWSITEMS)

      if (hasUserSuppliedALocation) { // TODO split into seperate model
        val radius = getLocationSearchRadius(request)

        val eventualRelatedTagsForLocation = relatedTagsService.getRelatedTagsForLocation(userSuppliedPlace, radius, Option(loggedInUserFilter.getLoggedInUser))
        val eventualPublishersForLocation = relatedTagsService.getRelatedPublishersForLocation(userSuppliedPlace, radius, Option(loggedInUserFilter.getLoggedInUser))
        for {
          relatedTagLinks <- eventualRelatedTagsForLocation
          relatedPublisherLinks <- eventualPublishersForLocation

        } yield {
          val latLong = userSuppliedPlace.getLatLong
          val totalNearbyCount = contentRetrievalService.getNewsitemsNearCount(latLong, radius, loggedInUser = Option(loggedInUserFilter.getLoggedInUser))
          if (startIndex > totalNearbyCount) {
            None
          }

          populatePagination(mv, startIndex, totalNearbyCount, MAX_NEWSITEMS)
          mv.addObject("location", userSuppliedPlace)
          mv.addObject("radius", radius)
          mv.addObject(MAIN_CONTENT, contentRetrievalService.getNewsitemsNear(latLong, radius, startIndex, MAX_NEWSITEMS, Option(loggedInUserFilter.getLoggedInUser)).asJava)

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
          geocodedNewsitems <- contentRetrievalService.getGeocodedNewsitems(startIndex, MAX_NEWSITEMS, Option(loggedInUserFilter.getLoggedInUser))
        } yield {

          val totalGeotaggedCount = contentRetrievalService.getGeocodedNewitemsCount(Option(loggedInUserFilter.getLoggedInUser))
          if (startIndex > totalGeotaggedCount) {
            None
          }
          populatePagination(mv, startIndex, totalGeotaggedCount, MAX_NEWSITEMS)

          mv.addObject("heading", "Geotagged newsitems")
          mv.addObject(MAIN_CONTENT, Await.result(contentRetrievalService.getGeocodedNewsitems(startIndex, MAX_NEWSITEMS, Option(loggedInUserFilter.getLoggedInUser)), TenSeconds).asJava)
          commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForGeotagged, rssUrlBuilder.getRssUrlForGeotagged)
          Some(mv)
        }
      }

    } else {
      Future.successful(None)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    mv.addObject("latest_newsitems", Await.result(contentRetrievalService.getLatestNewsitems(5, loggedInUser = Option(loggedInUserFilter.getLoggedInUser)), TenSeconds).asJava)
  }

  def getViewName(mv: ModelAndView): String = "geocoded"

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

}
