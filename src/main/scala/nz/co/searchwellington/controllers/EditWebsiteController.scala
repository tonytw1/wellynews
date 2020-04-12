package nz.co.searchwellington.controllers

import javax.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.forms.EditWebsite
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
class EditWebsiteController @Autowired()(contentUpdateService: ContentUpdateService,
                                         mongoRepository: MongoRepository,
                                         urlWordsGenerator: UrlWordsGenerator, urlBuilder: UrlBuilder,
                                         loggedInUserFilter: LoggedInUserFilter, tagDAO: TagDAO) extends ReasonableWaits with AcceptancePolicyOptions with Errors {

  private val log = Logger.getLogger(classOf[EditWebsiteController])

  @RequestMapping(value = Array("/edit-website/{id}"), method = Array(RequestMethod.GET))
  def prompt(@PathVariable id: String): ModelAndView = {
    Option(loggedInUserFilter.getLoggedInUser).map { loggedInUser =>
      getWebsiteById(id).map { w =>
        val editWebsite = new EditWebsite()
        editWebsite.setTitle(w.title.getOrElse(""))
        editWebsite.setUrl(w.page.getOrElse(""))
        editWebsite.setDescription(w.description.getOrElse(""))
        w.geocode.map { g =>
          editWebsite.setGeocode(g.getAddress)
          val osmId = g.osmId.flatMap { i =>
            g.osmType.map { t =>
              i + t
            }
          }
          editWebsite.setSelectedGeocode(osmId.getOrElse(""))
        }

        val usersTags = w.resource_tags.filter(_.user_id == loggedInUser._id)

        import scala.collection.JavaConverters._
        editWebsite.setTags(usersTags.map(_.tag_id.stringify).asJava)

        renderEditForm(w, editWebsite)


      }.getOrElse {
        NotFound
      }

    }.getOrElse {
      NotFound // TODO logged in user
    }
  }

  @RequestMapping(value = Array("/edit-website/{id}"), method = Array(RequestMethod.POST))
  def submit(@PathVariable id: String, @Valid @ModelAttribute("editWebsite") editWebsite: EditWebsite, result: BindingResult): ModelAndView = {

    Option(loggedInUserFilter.getLoggedInUser).map { loggedInUser =>

      getWebsiteById(id).map { w =>
        if (result.hasErrors) {
          log.warn("Edit website submission has errors: " + result)
          renderEditForm(w, editWebsite)

        } else {
          log.info("Got valid edit website submission: " + editWebsite)

          val geocode = Option(editWebsite.getGeocode).flatMap { address =>
            Option(editWebsite.getSelectedGeocode).flatMap { osmId =>
              if (osmId.nonEmpty) {
                val id = osmId.split("/")(0).toLong
                val `type` = osmId.split("/")(1)
                Some(Geocode(address = Some(address), osmId = Some(id), osmType = Some(`type`)))
              } else {
                None
              }
            }
          }

          import scala.collection.JavaConverters._
          val taggings = Await.result(tagDAO.loadTagsById(editWebsite.getTags.asScala), TenSeconds).map { tag =>
            Tagging(tag_id = tag._id, user_id = loggedInUser._id)
          }

          val updatedWebsite = w.copy(
            title = Some(editWebsite.getTitle),
            page = Some(editWebsite.getUrl),
            description = Some(editWebsite.getDescription),
            geocode = geocode,
            held = submissionShouldBeHeld(loggedInUser)
          ).withTags(taggings)


          contentUpdateService.update(updatedWebsite)
          log.info("Updated website: " + updatedWebsite)

          new ModelAndView(new RedirectView(urlBuilder.getPublisherUrl(updatedWebsite.url_words.get)))
        }

      }.getOrElse(NotFound)

    }.getOrElse {
      NotFound // TODO logged in user
    }
  }

  private def getWebsiteById(id: String): Option[Website] = {
    Await.result(mongoRepository.getResourceById(id), TenSeconds).flatMap { r =>
      r match {
        case w: Website => Some(w)
        case _ => None
      }
    }
  }

  private def submissionShouldBeHeld(owner: User): Boolean = {
    !owner.isAdmin
  }

  private def renderEditForm(w: Website, editWebsite: EditWebsite): ModelAndView = {
    import scala.collection.JavaConverters._
    new ModelAndView("editWebsite").
      addObject("title", "Editing a website").
      addObject("website", w).
      addObject("editWebsite", editWebsite).
      addObject("tags", Await.result(tagDAO.getAllTags, TenSeconds).asJava)
  }

}