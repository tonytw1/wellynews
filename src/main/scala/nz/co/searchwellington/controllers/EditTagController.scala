package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.forms.EditTag
import nz.co.searchwellington.model.{Tag, UrlWordsGenerator}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import nz.co.searchwellington.views.Errors
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{PathVariable, RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller
class EditTagController @Autowired()(contentUpdateService: ContentUpdateService,
                                     mongoRepository: MongoRepository,
                                     urlWordsGenerator: UrlWordsGenerator, urlBuilder: UrlBuilder,
                                     loggedInUserFilter: LoggedInUserFilter) extends ReasonableWaits with Errors {

  private val log = Logger.getLogger(classOf[EditTagController])

  @RequestMapping(value = Array("/edit-tag/{id}"), method = Array(RequestMethod.GET))
  def prompt(@PathVariable id: String): ModelAndView = {
    Await.result(mongoRepository.getTagById(id), TenSeconds).map { tag =>
      val editTag = new EditTag()
      editTag.setDisplayName(tag.display_name)
      editTag.setDescription(tag.description.getOrElse(""))
      renderEditForm(tag, editTag)

    }.getOrElse {
      NotFound
    }
  }

  private def renderEditForm(tag: Tag, editTag: EditTag): ModelAndView = {
    new ModelAndView("editTag").
      addObject("tag", tag).
      addObject("editTag", editTag)
  }

}