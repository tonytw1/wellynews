package nz.co.searchwellington.controllers

import javax.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.forms.EditWebsite
import nz.co.searchwellington.model.{Geocode, UrlWordsGenerator, Website}
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

@Controller
class EditWebsiteController @Autowired()(contentUpdateService: ContentUpdateService,
                                         mongoRepository: MongoRepository,
                                         urlWordsGenerator: UrlWordsGenerator, urlBuilder: UrlBuilder,
                                         loggedInUserFilter: LoggedInUserFilter, tagDAO: TagDAO) extends ReasonableWaits with AcceptancePolicyOptions with Errors {

  private val log = Logger.getLogger(classOf[EditWebsiteController])

  @RequestMapping(value = Array("/edit-website/{id}"), method = Array(RequestMethod.GET))
  def prompt(@PathVariable id: String): ModelAndView = {

    Option(loggedInUserFilter.getLoggedInUser).map { loggedInUser =>

      Await.result(mongoRepository.getResourceById(id), TenSeconds).flatMap { r =>
        r match {
          case w: Website =>
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

            Some(renderEditForm(w, editWebsite))

          case _ =>
            log.info("Not a website")
            None
        }

      }.getOrElse {
        NotFound
      }

    }.getOrElse {
      NotFound  // TODO logged in user
    }
  }

  @RequestMapping(value = Array("/edit-website/{id}"), method = Array(RequestMethod.POST))
  def submit(@PathVariable id: String, @Valid @ModelAttribute("editWebsite") editWebsite: EditWebsite, result: BindingResult): ModelAndView = {
    Await.result(mongoRepository.getResourceById(id), TenSeconds).flatMap { r =>
      r match {
        case w: Website =>
          if (result.hasErrors) {
            log.warn("Edit website submission has errors: " + result)
            return renderEditForm(w, editWebsite)

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

            val updatedWebsite = w.copy(
              title = Some(editWebsite.getTitle),
              page = Some(editWebsite.getUrl),
              description = Some(editWebsite.getDescription),
              geocode = geocode
            )

            contentUpdateService.update(updatedWebsite)
            log.info("Updated website: " + updatedWebsite)

            Some(new ModelAndView(new RedirectView(urlBuilder.getPublisherUrl(updatedWebsite.url_words.get))))
          }
        case _ =>
          None
      }

    }.getOrElse(NotFound)
  }

  private def renderEditForm(w: Website, editWebsite: EditWebsite): ModelAndView = {
    val mv = new ModelAndView("editWebsite")
    mv.addObject("website", w)
    mv.addObject("editWebsite", editWebsite)
    import scala.collection.JavaConverters._
    mv.addObject("tags", Await.result(tagDAO.getAllTags, TenSeconds).asJava)
    mv
  }

}