package nz.co.searchwellington.controllers

import javax.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.forms.NewTag
import nz.co.searchwellington.model.{Tag, UrlWordsGenerator, User}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.{BindingResult, ObjectError}
import org.springframework.web.bind.annotation.{ModelAttribute, RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller
class NewTagController @Autowired()(mongoRepository: MongoRepository,
                                    urlWordsGenerator: UrlWordsGenerator,
                                    urlBuilder: UrlBuilder,
                                    val loggedInUserFilter: LoggedInUserFilter) extends ReasonableWaits
  with AcceptancePolicyOptions with InputParsing with RequiringLoggedInUser {

  private val log = Logger.getLogger(classOf[NewTagController])

  @RequestMapping(value = Array("/new-tag"), method = Array(RequestMethod.GET))
  def prompt(): ModelAndView = {
    def showAddTagPrompt(loggedInUser: User): ModelAndView = {
      renderForm(new NewTag())
    }

    requiringAdminUser(showAddTagPrompt)
  }

  @RequestMapping(value = Array("/new-tag"), method = Array(RequestMethod.POST))
  def submit(@Valid @ModelAttribute("newTag") newTag: NewTag, result: BindingResult): ModelAndView = {

    def submitNewTag(loggedInUser: User): ModelAndView = {
      if (!result.hasErrors) {
        log.info("Got valid new tag submission: " + newTag)

        if (!result.hasErrors) {
          val tag = Tag(
            display_name = newTag.getDisplayName,
            description = optionalInputString(newTag.getDescription)
          )

          val urlWordsFromDisplayName = urlWordsGenerator.makeUrlWordsForTag(tag)

          val existingTagWithSameUrlWords = Await.result(mongoRepository.getTagByUrlWords(urlWordsFromDisplayName), TenSeconds)
          if (existingTagWithSameUrlWords.isEmpty) {
            val withUrlWords = tag.copy(name = urlWordsFromDisplayName)
            Await.result(mongoRepository.saveTag(withUrlWords), TenSeconds)
            log.info("Created tag: " + withUrlWords)

            new ModelAndView(new RedirectView(urlBuilder.getTagUrl(withUrlWords)))

          } else {
            result.addError(new ObjectError("displayName", "Found existing feed with same URL words"))
            renderForm(newTag)
          }

        } else {
          log.warn("New tag submission has errors: " + result)
          renderForm(newTag)
        }

      } else {
        log.warn("New tag submission has errors: " + result)
        renderForm(newTag)
      }
    }

    requiringAdminUser(submitNewTag)
  }

  private def renderForm(newTag: NewTag): ModelAndView = {
    new ModelAndView("newTag").
      addObject("heading", "Adding a tag").
      addObject("newTag", newTag)
  }

}