package nz.co.searchwellington.controllers

import javax.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.forms.EditNewsitem
import nz.co.searchwellington.geocoding.osm.CachingNominatimResolveOsmIdService
import nz.co.searchwellington.model._
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import nz.co.searchwellington.views.Errors
import org.apache.log4j.Logger
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
                                          urlBuilder: UrlBuilder,
                                          val loggedInUserFilter: LoggedInUserFilter,
                                          tagDAO: TagDAO,
                                          val cachingNominatimResolveOsmIdService: CachingNominatimResolveOsmIdService
                                         ) extends ReasonableWaits with AcceptancePolicyOptions with Errors with GeotagParsing with RequiringLoggedInUser {

  private val log = Logger.getLogger(classOf[EditNewsitemController])

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
  def submit(@PathVariable id: String, @Valid @ModelAttribute("editNewsitem") formObject: EditNewsitem, result: BindingResult): ModelAndView = {
    def handleSubmission(loggedInUser: User): ModelAndView = {
      getNewsitemById(id).map { w =>
        if (result.hasErrors) {
          log.warn("Edit newsitem submission has errors: " + result)
          renderEditForm(w, formObject)

        } else {
          log.info("Got valid edit newsitem submission: " + formObject)

          val geocode = Option(formObject.getGeocode).flatMap { address =>
            Option(formObject.getSelectedGeocode).flatMap { osmId =>
              parseGeotag(address, osmId)
            }
          }

          import scala.collection.JavaConverters._
          val taggings = Await.result(tagDAO.loadTagsById(formObject.getTags.asScala), TenSeconds).map { tag =>
            Tagging(tag_id = tag._id, user_id = loggedInUser._id)
          }

          val updated = w.copy(
            title = Some(formObject.getTitle),
            page = Some(formObject.getUrl),
            description = Some(formObject.getDescription),
            geocode = geocode,
            held = submissionShouldBeHeld(loggedInUser)
          ).withTags(taggings)


          contentUpdateService.update(updated)

          new ModelAndView(new RedirectView("TODO"))
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
    formObject.setUrl(n.page.getOrElse(""))
    formObject.setDescription(n.description.getOrElse(""))

    n.geocode.map { g =>
      formObject.setGeocode(g.getAddress)
      val osmId = g.osmId.map { i =>
        i.id + i.`type`
      }
      formObject.setSelectedGeocode(osmId.getOrElse(""))
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
      addObject("editNewitem", formObject).
      addObject("tags", Await.result(tagDAO.getAllTags, TenSeconds).asJava)
  }

}