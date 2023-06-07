package nz.co.searchwellington.controllers

import io.opentelemetry.api.trace.Span
import jakarta.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.forms.NewTag
import nz.co.searchwellington.model.{Tag, UrlWordsGenerator, User}
import nz.co.searchwellington.repositories.{ContentRetrievalService, TagDAO}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.{BindingResult, ObjectError}
import org.springframework.web.bind.annotation.{GetMapping, ModelAttribute, PostMapping}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller
class NewTagController @Autowired()(mongoRepository: MongoRepository,
                                    urlWordsGenerator: UrlWordsGenerator,
                                    urlBuilder: UrlBuilder,
                                    val contentRetrievalService: ContentRetrievalService,
                                    val tagDAO: TagDAO,
                                    val loggedInUserFilter: LoggedInUserFilter) extends EditScreen
  with ReasonableWaits with AcceptancePolicyOptions with InputParsing with RequiringLoggedInUser {

  private val log = LogFactory.getLog(classOf[NewTagController])

  @GetMapping(Array("/new-tag"))
  def prompt(): ModelAndView = {
    implicit val currentSpan: Span = Span.current()

    def showAddTagPrompt(loggedInUser: User): ModelAndView = {
      renderForm(new NewTag(), loggedInUser)
    }

    requiringAdminUser(showAddTagPrompt)
  }

  @PostMapping(Array("/new-tag"))
  def submit(@Valid @ModelAttribute("formObject") newTag: NewTag, result: BindingResult): ModelAndView = {

    def submitNewTag(loggedInUser: User): ModelAndView = {
      implicit val currentSpan: Span = Span.current()

      if (!result.hasErrors) {
        log.info("Got valid new tag submission: " + newTag)

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
          result.addError(new ObjectError("displayName", "Found existing tag with same URL words"))
          renderForm(newTag, loggedInUser)
        }

      } else {
        log.warn("New tag submission has errors: " + result)
        renderForm(newTag, loggedInUser)
      }
    }

    requiringAdminUser(submitNewTag)
  }

  private def renderForm(newTag: NewTag, loggedInUser: User)(implicit currentSpan: Span): ModelAndView = {
    editScreen("newTag", "Adding a tag", Some(loggedInUser)).
      addObject("formObject", newTag)
  }

}