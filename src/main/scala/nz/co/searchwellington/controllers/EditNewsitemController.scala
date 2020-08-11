package nz.co.searchwellington.controllers

import javax.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.forms.EditNewsitem
import nz.co.searchwellington.geocoding.osm.CachingNominatimResolveOsmIdService
import nz.co.searchwellington.model._
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{HandTaggingService, TagDAO}
import nz.co.searchwellington.views.Errors
import org.apache.log4j.Logger
import org.joda.time.format.ISODateTimeFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.{ModelAttribute, PathVariable, RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller
class EditNewsitemController @Autowired()(contentUpdateService: ContentUpdateService,
                                          mongoRepository: MongoRepository,
                                          val loggedInUserFilter: LoggedInUserFilter,
                                          tagDAO: TagDAO,
                                          val cachingNominatimResolveOsmIdService: CachingNominatimResolveOsmIdService,
                                          handTaggingService: HandTaggingService)
  extends ReasonableWaits with AcceptancePolicyOptions with Errors with GeotagParsing with RequiringLoggedInUser {

  private val log = Logger.getLogger(classOf[EditNewsitemController])

  private val formDateFormat = ISODateTimeFormat.basicDate

  @RequestMapping(value = Array("/edit-newsitem/{id}"), method = Array(RequestMethod.GET))
  def prompt(@PathVariable id: String): ModelAndView = {
    def showForm(loggedInUser: User): ModelAndView = {
      getNewsitemById(id).map { newsitem =>
        renderEditForm(newsitem, mapToForm(newsitem, loggedInUser))
      }.getOrElse {
        log.info("No newsitem found for id: " + id)
        NotFound
      }
    }

    requiringAdminUser(showForm)
  }

  @RequestMapping(value = Array("/edit-newsitem/{id}"), method = Array(RequestMethod.POST))
  def submit(@PathVariable id: String, @Valid @ModelAttribute("formObject") formObject: EditNewsitem, result: BindingResult): ModelAndView = {
    def handleSubmission(loggedInUser: User): ModelAndView = {
      getNewsitemById(id).map { newsitem =>
        if (result.hasErrors) {
          log.warn("Edit newsitem submission has errors: " + result)
          renderEditForm(newsitem, formObject)

        } else {
          log.info("Got valid edit newsitem submission: " + formObject)

          val geocode = Option(formObject.getGeocode).flatMap { address =>
            Option(formObject.getSelectedGeocode).flatMap { osmId =>
              parseGeotag(address, osmId)
            }
          }

          val date = formDateFormat.parseLocalDate(formObject.getDate).toDate

          import scala.collection.JavaConverters._
          val submittedTags = Await.result(tagDAO.loadTagsById(formObject.getTags.asScala), TenSeconds).toSet

          val updated = handTaggingService.setUsersTagging(loggedInUser, submittedTags.map(_._id), newsitem.copy(
            title = Some(formObject.getTitle),
            page = formObject.getUrl,
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

  private def submissionShouldBeHeld(owner: User): Boolean = {
    !owner.isAdmin
  }

  private def mapToForm(n: Newsitem, loggedInUser: User): EditNewsitem = {
    val formObject = new EditNewsitem()
    formObject.setTitle(n.title.getOrElse(""))
    formObject.setUrl(n.page)
    formObject.setDescription(n.description.getOrElse(""))

    n.geocode.map { g =>
      formObject.setGeocode(g.getAddress)
      val osmId = g.osmId.map { i =>
        i.id + i.`type`
      }
      formObject.setSelectedGeocode(osmId.getOrElse(""))
    }

    n.date.map { d =>
      formObject.setDate(formDateFormat.print(d.getTime))
    }

    val usersTags = n.resource_tags.filter(_.user_id == loggedInUser._id)
    import scala.collection.JavaConverters._
    formObject.setTags(usersTags.map(_.tag_id.stringify).asJava)

    formObject
  }


  private def renderEditForm(n: Newsitem, formObject: EditNewsitem): ModelAndView = {
    import scala.collection.JavaConverters._
    new ModelAndView("editNewsitem").
      addObject("title", "Editing a newsitem").
      addObject("newsitem", n).
      addObject("formObject", formObject).
      addObject("tags", Await.result(tagDAO.getAllTags, TenSeconds).asJava)
  }

}