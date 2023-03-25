package nz.co.searchwellington.controllers

import com.google.common.base.Strings
import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.PlaceToGeocodeMapper
import nz.co.searchwellington.geocoding.osm.OsmIdParser
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.geo.Geocode
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{HandTaggingDAO, TagDAO}
import nz.co.searchwellington.utils.{StringWrangling, UrlFilters}
import org.apache.commons.lang3.StringUtils
import org.apache.commons.logging.LogFactory
import org.apache.commons.text.StringEscapeUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.model.OsmId

import scala.jdk.CollectionConverters.CollectionHasAsScala
@Component class SubmissionProcessingService @Autowired()(tagDAO: TagDAO,
                                                          tagVoteDAO: HandTaggingDAO,
                                                          osmIdParser: OsmIdParser, placeToGeocodeMapper: PlaceToGeocodeMapper,
                                                          mongoRepository: MongoRepository) extends ReasonableWaits with StringWrangling {

  private val log = LogFactory.getLog(classOf[SubmissionProcessingService])

  private val REQUEST_TITLE_NAME = "title"
  private val REQUEST_DATE_NAME = "date"
  private val REQUEST_DESCRIPTION_NAME = "description"
  private val REQUEST_SELECTED_GEOCODE = "selectedGeocode"
  private val REQUEST_EMBARGO_DATE_NAME = "embargo_date"

  def processImage(request: HttpServletRequest, editResource: Newsitem, loggedInUser: User): Unit = {
    val image = request.getAttribute("image").asInstanceOf[Image]
    // editResource.setImage(image)
  }

  def processGeocode(request: HttpServletRequest): Geocode = {  // TODO make an Option
    if (!Strings.isNullOrEmpty(request.getParameter(REQUEST_SELECTED_GEOCODE))) {

      val osmIdString: String = new String(request.getParameter(REQUEST_SELECTED_GEOCODE).trim)
      val osmId: OsmId = osmIdParser.parseOsmId(osmIdString)
      //val resolvedPlace: Place = nominatimGeocodeService.resolveOsmId(osmId)

     // log.info("Selected geocode " + osmIdString + " resolved to: " + resolvedPlace)

      /*
      Option(resolvedPlace).map { p =>
        placeToGeocodeMapper.mapPlaceToGeocode(p)

      }.getOrElse {
        log.warn("Could not resolve OSM id: " + osmId)
        null
      }
       */
      null

    } else {
      null
    }
  }

  def processEmbargoDate(request: HttpServletRequest, editResource: Resource): Unit = {
    //editResource.setEmbargoedUntil(request.getAttribute(REQUEST_EMBARGO_DATE_NAME).asInstanceOf[Date])
  }

  def processDescription(request: HttpServletRequest, editResource: Resource): Unit = {
    var description = request.getParameter(REQUEST_DESCRIPTION_NAME)
    if (description != null) {
      description = StringEscapeUtils.unescapeHtml4(description)
      description = UrlFilters.stripHtml(description)
    }
    //editResource.setDescription(description)
  }

  def processHeld(request: HttpServletRequest, editResource: Resource): Unit = {
    if (request.getParameter("has_held") != null) {
      if (request.getParameter("held") != null) {
      //  editResource.setHeld(true)
      } else {
       // editResource.setHeld(false)
      }
    }
  }

  def processTags(request: HttpServletRequest, editResource: Resource, user: User): Unit = {
    log.info("Processing tags")
    if (request.getParameter("has_tag_select") != null) {
      processTagSelect(request, editResource, user)
    }
    else {
      log.debug("No additional tag string found.")
    }
  }

  @SuppressWarnings(Array("unchecked"))
  private def processTagSelect(request: HttpServletRequest, editResource: Resource, user: User): Unit = {
    log.info("Processing tag select")
    if (request.getAttribute("tags") != null) {
      val requestTagsList = request.getAttribute("tags").asInstanceOf[java.util.List[Tag]].asScala.toSeq
      log.debug("Found tags on request: " + requestTagsList)
      log.info("Found " + requestTagsList.size + " tags on the request")
      //tagVoteDAO.setUsersTagVotesForResource(editResource, user, requestTagsList.toSet)

    } else {
      log.info("No tags request attribute seen; clearing users tag votes")
      //tagVoteDAO.setUsersTagVotesForResource(editResource, user, Set())
    }
  }

  def processPublisher(request: HttpServletRequest, editResource: Resource): Unit = {
    val isPublishedResource: Boolean = editResource.isInstanceOf[PublishedResource]
    if (isPublishedResource) {
      if (request.getParameter("publisherName") != null && !(request.getParameter("publisherName") == "")) {
        val publisherName: String = request.getParameter("publisherName")
        // val publisher: Website = resourceDAO.getPublisherByName(publisherName).asInstanceOf[Website]
        //if (publisher != null) {
         // log.info("Found publisher: " + publisher.title)
          // (editResource.asInstanceOf[PublishedResource]).setPublisher(publisher)
       // }
      }
    }
  }

  private def isValidTagName(field: String): Boolean = {
    field != null && field.nonEmpty && field.matches("[a-zA-Z0-9]*")
  }

  private def cleanTagName(i: String): String = {
    var cleaned = StringUtils.strip(i)
    cleaned = StringUtils.remove(cleaned, " ")
    cleaned.toLowerCase.trim
  }

}
