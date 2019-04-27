package nz.co.searchwellington.controllers

import javax.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.forms.EditWebsite
import nz.co.searchwellington.model.{UrlWordsGenerator, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
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
                                         loggedInUserFilter: LoggedInUserFilter) extends ReasonableWaits with AcceptancePolicyOptions {

  private val log = Logger.getLogger(classOf[EditWebsiteController])

  private val NotFound = new ModelAndView("404")  // TODO

  @RequestMapping(value = Array("/edit-website/{id}"), method = Array(RequestMethod.GET))
  def prompt(@PathVariable id: String): ModelAndView = {
    log.info("Edit website")
    Await.result(mongoRepository.getResourceById(id), TenSeconds).flatMap { r =>
      r match {
        case w: Website =>
          val editWebsite = new EditWebsite()
          editWebsite.setTitle(w.title.getOrElse(""))
          editWebsite.setUrl(w.page.getOrElse(""))

          w.geocode.map { g =>
            editWebsite.setGeocode(g.getAddress)
            editWebsite.setSelectedGeocode(g.getOsmId + g.getType)
          }

          Some(renderEditForm(w, editWebsite))

        case _ =>
          log.info("Not a website")
          None
      }

    }.getOrElse{
      NotFound
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

            val updatedWebsite = w.copy(
              title = Some(editWebsite.getTitle),
              page = Some(editWebsite.getUrl)
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
    return mv
  }

}