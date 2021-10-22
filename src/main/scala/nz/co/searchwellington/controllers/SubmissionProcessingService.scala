package nz.co.searchwellington.controllers

import com.google.common.base.Strings
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.submission.UrlProcessor
import nz.co.searchwellington.feeds.PlaceToGeocodeMapper
import nz.co.searchwellington.geocoding.osm.OsmIdParser
import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{HandTaggingDAO, HibernateResourceDAO, TagDAO}
import nz.co.searchwellington.utils.{StringWrangling, UrlFilters}
import org.apache.commons.lang.{StringEscapeUtils, StringUtils}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.model.OsmId

import java.util.{Calendar, Date}
import javax.servlet.http.HttpServletRequest
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Component class SubmissionProcessingService @Autowired()(tagDAO: TagDAO,
                                                          tagVoteDAO: HandTaggingDAO, resourceDAO: HibernateResourceDAO, urlProcessor: UrlProcessor,
                                                          osmIdParser: OsmIdParser, placeToGeocodeMapper: PlaceToGeocodeMapper,
                                                          mongoRepository: MongoRepository) extends ReasonableWaits with StringWrangling {

  private val log = Logger.getLogger(classOf[SubmissionProcessingService])

  private val REQUEST_TITLE_NAME = "title"
  private val REQUEST_DATE_NAME = "date"
  private val REQUEST_DESCRIPTION_NAME = "description"
  private val REQUEST_SELECTED_GEOCODE = "selectedGeocode"
  private val REQUEST_EMBARGO_DATE_NAME = "embargo_date"

  def processTitle(req: HttpServletRequest, editResource: Resource) {
    if (req.getParameter(REQUEST_TITLE_NAME) != null) {
      var title = new String(req.getParameter(REQUEST_TITLE_NAME))
      title = UrlFilters.trimWhiteSpace(title)
      title = UrlFilters.stripHtml(title)
      val flattenedTitle = lowerCappedSentence(title)
      if (!(flattenedTitle == title)) {
        title = flattenedTitle
        log.info("Flatten capitalised sentence to '" + title + "'")
      }
      //editResource.setName(title)
    }
  }

  def processUrl(request: HttpServletRequest, editResource: Resource) {
    urlProcessor.process(request, editResource)
  }

  def processImage(request: HttpServletRequest, editResource: Newsitem, loggedInUser: User) {
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

  def processDate(request: HttpServletRequest, editResource: Resource) {
    editResource.setDate(request.getAttribute(REQUEST_DATE_NAME).asInstanceOf[Date])
    if (editResource.date.isEmpty && editResource.id == 0) {
      editResource.setDate(Calendar.getInstance.getTime)
    }
  }

  def processEmbargoDate(request: HttpServletRequest, editResource: Resource) {
    editResource.setEmbargoedUntil(request.getAttribute(REQUEST_EMBARGO_DATE_NAME).asInstanceOf[Date])
  }

  def processDescription(request: HttpServletRequest, editResource: Resource) {
    var description = request.getParameter(REQUEST_DESCRIPTION_NAME)
    if (description != null) {
      description = StringEscapeUtils.unescapeHtml(description)
      description = UrlFilters.stripHtml(description)
    }
    editResource.setDescription(description)
  }

  def processHeld(request: HttpServletRequest, editResource: Resource) {
    if (request.getParameter("has_held") != null) {
      if (request.getParameter("held") != null) {
        editResource.setHeld(true)
      } else {
        editResource.setHeld(false)
      }
    }
  }

  def processTags(request: HttpServletRequest, editResource: Resource, user: User) {
    log.info("Processing tags")
    if (request.getParameter("has_tag_select") != null) {
      processTagSelect(request, editResource, user)
    }
    if (request.getParameter("additional_tags") != null) {
      processAdditionalTags(request, editResource, user)
    }
    else {
      log.debug("No additional tag string found.")
    }
  }

  @SuppressWarnings(Array("unchecked")) private def processTagSelect(request: HttpServletRequest, editResource: Resource, user: User) {
    log.info("Processing tag select")
    if (request.getAttribute("tags") != null) {
      import scala.collection.JavaConversions._
      val requestTagsList: Seq[Tag] = request.getAttribute("tags").asInstanceOf[java.util.List[Tag]]
      log.debug("Found tags on request: " + requestTagsList)
      log.info("Found " + requestTagsList.size + " tags on the request")
      //tagVoteDAO.setUsersTagVotesForResource(editResource, user, requestTagsList.toSet)

    } else {
      log.info("No tags request attribute seen; clearing users tag votes")
      //tagVoteDAO.setUsersTagVotesForResource(editResource, user, Set())
    }
  }

  def processPublisher(request: HttpServletRequest, editResource: Resource) {
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

  private def processAdditionalTags(request: HttpServletRequest, editResource: Resource, user: User) {
    val additionalTagString: String = request.getParameter("additional_tags").trim
    log.debug("Found additional tag string: " + additionalTagString)
    val fields: Array[String] = additionalTagString.split(",")
    if (fields.length > 0) {
      var i: Int = 0
      while (i < fields.length) {
        {
          var field: String = fields(i).trim
          val displayName: String = field
          field = cleanTagName(field)
          log.debug("Wants additional tag: " + field)
          if (isValidTagName(field)) {

            Await.result(mongoRepository.getTagByUrlWords(field), TenSeconds).map { existingTag =>
              log.debug("Found an existing tag in the additional list: " + existingTag.getName + "; adding.")
              //tagVoteDAO.addTag(user, existingTag, editResource)

            }.getOrElse {
              log.debug("Tag '" + field + "' is a new tag. Needs to be created.")
              val newTag = Tag(name = field, display_name = displayName)
              Await.result(mongoRepository.saveTag(newTag), TenSeconds)
              //tagVoteDAO.addTag(user, newTag, editResource)
            }
          }
          else {
            log.debug("Ignoring invalid tag name: " + field)
          }
        }
        ({
          i += 1;
          i - 1
        })
      }
    }
  }

  private def isValidTagName(field: String): Boolean = {
    field != null && field.length > 0 && field.matches("[a-zA-Z0-9]*")
  }

  private def cleanTagName(i: String): String = {
    var cleaned = StringUtils.strip(i)
    cleaned = StringUtils.remove(cleaned, " ")
    cleaned.toLowerCase.trim
  }

}
