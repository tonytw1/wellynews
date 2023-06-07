package nz.co.searchwellington.controllers

import jakarta.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.submission.{EndUserInputs, GeotagParsing}
import nz.co.searchwellington.forms.EditNewsitem
import nz.co.searchwellington.geocoding.osm.GeoCodeService
import nz.co.searchwellington.model._
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{HandTaggingService, TagDAO}
import nz.co.searchwellington.urls.UrlCleaner
import nz.co.searchwellington.views.Errors
import org.apache.commons.logging.LogFactory
import org.joda.time.format.ISODateTimeFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.{GetMapping, ModelAttribute, PathVariable, PostMapping}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

@Controller
class EditNewsitemController @Autowired()(contentUpdateService: ContentUpdateService,
                                          mongoRepository: MongoRepository,
                                          val loggedInUserFilter: LoggedInUserFilter,
                                          val tagDAO: TagDAO,
                                          val geocodeService: GeoCodeService,
                                          handTaggingService: HandTaggingService,
                                          val urlCleaner: UrlCleaner)
  extends EditScreen with ReasonableWaits with AcceptancePolicyOptions with Errors with GeotagParsing with RequiringLoggedInUser
  with EndUserInputs with HeldSubmissions {

  private val log = LogFactory.getLog(classOf[EditNewsitemController])

  private val formDateFormat = ISODateTimeFormat.basicDate

  @GetMapping(value = Array("/edit-newsitem/{id}"))
  def prompt(@PathVariable id: String): ModelAndView = {
    def showForm(loggedInUser: User): ModelAndView = {
      getNewsitemById(id).map { newsitem =>
        renderEditForm(newsitem, mapToForm(newsitem, loggedInUser), loggedInUser)
      }.getOrElse {
        log.info("No newsitem found for id: " + id)
        NotFound
      }
    }

    requiringAdminUser(showForm)
  }

  @PostMapping(Array("/edit-newsitem/{id}"))
  def submit(@PathVariable id: String, @Valid @ModelAttribute("formObject") formObject: EditNewsitem, result: BindingResult): ModelAndView = {
    def handleSubmission(loggedInUser: User): ModelAndView = {
      getNewsitemById(id).map { newsitem =>
        if (result.hasErrors) {
          log.warn("Edit newsitem submission has errors: " + result)
          renderEditForm(newsitem, formObject, loggedInUser)

        } else {
          log.info("Got valid edit newsitem submission: " + formObject)

          val geocode = Option(formObject.getGeocode).flatMap { address =>
            Option(formObject.getOsm).flatMap { osmId =>
              parseGeotag(address, osmId)
            }
          }

          val date = formDateFormat.parseLocalDate(formObject.getDate).toDate

          val submittedTags = Await.result(tagDAO.loadTagsById(formObject.getTags.asScala.toSeq), TenSeconds).toSet

          val publisherName = if (formObject.getPublisher.trim.nonEmpty) {
            Some(formObject.getPublisher.trim)
          } else {
            None
          }
          val publisher = publisherName.flatMap { publisherName =>
            Await.result(mongoRepository.getWebsiteByName(publisherName), TenSeconds)
          }

          val updated = handTaggingService.setUsersTagging(loggedInUser, submittedTags.map(_._id), newsitem.copy(
            title = processTitle(formObject.getTitle),
            page = formObject.getUrl,
            publisher = publisher.map(_._id),
            description = Some(formObject.getDescription),
            date = Some(date),
            geocode = geocode,
            held = submissionShouldBeHeld(loggedInUser)
          ))

          contentUpdateService.update(updated)

          new ModelAndView(new RedirectView("/newsitem/" + newsitem.id)) // TODO Url builder
        }

      }.getOrElse(NotFound)
    }

    requiringAdminUser(handleSubmission)
  }

  private def getNewsitemById(id: String): Option[Newsitem] = {
    Await.result(mongoRepository.getResourceById(id), TenSeconds).flatMap { r =>
      r match {
        case n: Newsitem => Some(n)
        case _ => None
      }
    }
  }

  private def mapToForm(n: Newsitem, loggedInUser: User): EditNewsitem = {
    val eventualPublisher = n.publisher.map { pid =>
      mongoRepository.getResourceByObjectId(pid).map { ro =>
        ro.flatMap { r =>
          r match {
            case w: Website => Some(w)
            case _ => None
          }
        }
      }

    }.getOrElse(Future.successful(None))
    val publisherName= Await.result(eventualPublisher, TenSeconds).map(p => p.title).getOrElse("")

    val formObject = new EditNewsitem()
    formObject.setTitle(n.title)
    formObject.setUrl(n.page)
    formObject.setPublisher(publisherName)
    formObject.setDescription(n.description.getOrElse(""))

    n.geocode.foreach { g =>
      formObject.setGeocode(g.getAddress)
      val osmId = g.osmId.map(osmToString)
      formObject.setOsm(osmId.getOrElse(""))
    }

    n.date.foreach { d =>
      formObject.setDate(formDateFormat.print(d.getTime))
    }

    val usersTags = n.resource_tags.filter(_.user_id == loggedInUser._id)
    formObject.setTags(usersTags.map(_.tag_id.stringify).asJava)

    formObject
  }


  private def renderEditForm(n: Newsitem, formObject: EditNewsitem, loggedInUser: User): ModelAndView = {
    editScreen("editNewsitem", "Editing a newsitem", Some(loggedInUser)).
      addObject("newsitem", n).
      addObject("formObject", formObject)
  }

}