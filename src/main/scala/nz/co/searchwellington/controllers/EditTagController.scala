package nz.co.searchwellington.controllers

import javax.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.forms.EditTag
import nz.co.searchwellington.model.{Tag, UrlWordsGenerator}
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
class EditTagController @Autowired()(contentUpdateService: ContentUpdateService,
                                     mongoRepository: MongoRepository, tagDAO: TagDAO,
                                     urlWordsGenerator: UrlWordsGenerator,
                                     urlBuilder: UrlBuilder,
                                     loggedInUserFilter: LoggedInUserFilter) extends ReasonableWaits with Errors {

  private val log = Logger.getLogger(classOf[EditTagController])

  @RequestMapping(value = Array("/edit-tag/{id}"), method = Array(RequestMethod.GET))
  def prompt(@PathVariable id: String): ModelAndView = {
    Await.result(mongoRepository.getTagById(id), TenSeconds).map { tag =>
      val editTag = new EditTag()
      editTag.setDisplayName(tag.display_name)
      editTag.setDescription(tag.description.getOrElse(""))
      editTag.setParent(tag.parent.map(_.stringify).orNull)
      renderEditForm(tag, editTag)

    }.getOrElse {
      NotFound
    }
  }

  @RequestMapping(value = Array("/edit-tag/{id}"), method = Array(RequestMethod.POST))
  def submit(@PathVariable id: String, @Valid @ModelAttribute("editTag") editTag: EditTag, result: BindingResult): ModelAndView = {
    Await.result(mongoRepository.getTagById(id), TenSeconds).map { tag =>
      if (result.hasErrors) {
        log.warn("Edit tag submission has errors: " + result)
        renderEditForm(tag, editTag)

      } else {
        val updatedTag = tag.copy(
          display_name = editTag.getDisplayName,
          description = Option(editTag.getDescription)
        )

        Await.result(mongoRepository.saveTag(updatedTag), TenSeconds)
        log.info("Updated feed: " + updatedTag)

        new ModelAndView(new RedirectView(urlBuilder.getTagUrl(tag)))
      }
    }.getOrElse {
      NotFound
    }
  }

  private def renderEditForm(tag: Tag, editTag: EditTag): ModelAndView = {
    import scala.collection.JavaConverters._
    val possibleParents = Await.result(tagDAO.getAllTags, TenSeconds).filterNot(_ == tag)
    new ModelAndView("editTag").
      addObject("tag", tag).
      addObject("parents", possibleParents.asJava).
      addObject("editTag", editTag)
  }

}