package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.controllers.{RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.filters.LocationParameterFilter
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.geo.model.Place

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Component class GeotaggedModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService,
                                                    urlBuilder: UrlBuilder,
                                                    rssUrlBuilder: RssUrlBuilder,
                                                    relatedTagsService: RelatedTagsService,
                                                    commonAttributesModelBuilder: CommonAttributesModelBuilder)
  extends ModelBuilder with CommonSizes with Pagination with ReasonableWaits {

  private val log = Logger.getLogger(classOf[GeotaggedModelBuilder])

  private val HOW_FAR_IS_CLOSE_IN_KILOMETERS = 1.0

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/geotagged(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: User): Future[Option[ModelAndView]] = {
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
        val latLong = userSuppliedPlace.getLatLong

        val eventualRelatedTagsForLocation = relatedTagsService.getRelatedTagsForLocation(userSuppliedPlace, radius, Option(loggedInUser))
        val eventualPublishersForLocation = relatedTagsService.getRelatedPublishersForLocation(userSuppliedPlace, radius, Option(loggedInUser))
        val eventualNewsitemsNearCount = contentRetrievalService.getNewsitemsNearCount(latLong, radius, loggedInUser = Option(loggedInUser))
        val eventualNewsitemsNear = contentRetrievalService.getNewsitemsNear(latLong, radius, startIndex, MAX_NEWSITEMS, Option(loggedInUser))
        for {
          relatedTagLinks <- eventualRelatedTagsForLocation
          relatedPublisherLinks <- eventualPublishersForLocation
          totalNearbyCount <- eventualNewsitemsNearCount
          newsitemsNear <- eventualNewsitemsNear

        } yield {
          if (startIndex > totalNearbyCount) {
            None
          }

          populatePagination(mv, startIndex, totalNearbyCount, MAX_NEWSITEMS)
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
          totalGeotaggedCount <- contentRetrievalService.getGeocodedNewitemsCount(Option(loggedInUser))
          geocodedNewsitems <- contentRetrievalService.getGeocodedNewsitems(startIndex, MAX_NEWSITEMS, Option(loggedInUser))
        } yield {
          if (startIndex > totalGeotaggedCount) {
            None
          }
          populatePagination(mv, startIndex, totalGeotaggedCount, MAX_NEWSITEMS)

          mv.addObject("heading", "Geotagged newsitems")
          mv.addObject(MAIN_CONTENT, geocodedNewsitems.asJava)
          commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForGeotagged, rssUrlBuilder.getRssUrlForGeotagged)
          Some(mv)
        }
      }

    } else {
      Future.successful(None)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: User) {
    mv.addObject("latest_newsitems", Await.result(contentRetrievalService.getLatestNewsitems(5, loggedInUser = Option(loggedInUser)), TenSeconds).asJava)
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
