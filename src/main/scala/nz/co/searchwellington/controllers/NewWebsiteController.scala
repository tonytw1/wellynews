package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.submission.EndUserInputs
import nz.co.searchwellington.forms.NewWebsite
import nz.co.searchwellington.model.{UrlWordsGenerator, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.{UrlBuilder, UrlCleaner}
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.{BindingResult, ObjectError}
import org.springframework.web.bind.annotation.{GetMapping, ModelAttribute, PostMapping}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import javax.servlet.http.HttpServletRequest
import javax.validation.Valid
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

@Controller
class NewWebsiteController @Autowired()(contentUpdateService: ContentUpdateService,
                                        mongoRepository: MongoRepository,
                                        urlWordsGenerator: UrlWordsGenerator, urlBuilder: UrlBuilder,
                                        val anonUserService: AnonUserService,
                                        val urlCleaner: UrlCleaner,
                                        tagDAO: TagDAO,
                                        loggedInUserFilter: LoggedInUserFilter) extends ReasonableWaits
  with EnsuredSubmitter with EndUserInputs {

  private val log = LogFactory.getLog(classOf[NewWebsiteController])

  @GetMapping(Array("/new-website"))
  def prompt(): ModelAndView = renderNewWebsiteForm(new NewWebsite())

  @PostMapping(Array("/new-website"))
  def submit(@Valid @ModelAttribute("formObject") newWebsite: NewWebsite, result: BindingResult, request: HttpServletRequest): ModelAndView = {
    val loggedInUser = loggedInUserFilter.getLoggedInUser

    if (result.hasErrors) {
      log.warn("New website submission has errors: " + result)
      renderNewWebsiteForm(newWebsite)

    } else {
      log.info("Got valid new website submission: " + newWebsite)
      val url = cleanUrl(newWebsite.getUrl).toOption.get.toExternalForm  // TODO error handling

      val w = Website(title = processTitle(newWebsite.getTitle),
        page = url,
        date = Some(DateTime.now.toDate),
      )
      val urlWords = urlWordsGenerator.makeUrlWordsFor(w)

      val eventualModelAndView = mongoRepository.getWebsiteByUrlwords(urlWords).flatMap { maybeExistingWebsite =>
        maybeExistingWebsite.fold {
          val website = w.copy(url_words = Some(urlWords))
          val submittingUser = ensuredSubmittingUser(loggedInUser)
          val withSubmittingUser = website.copy(owner = Some(submittingUser._id), held = submissionShouldBeHeld(Some(submittingUser)))
          contentUpdateService.create(withSubmittingUser).map { _ =>
            log.info("Created website: " + withSubmittingUser)
            setSignedInUser(request, submittingUser)
            new ModelAndView(new RedirectView(urlBuilder.getPublisherUrl(withSubmittingUser)))
          }
        } { existing =>
          log.warn("Found existing website site same url words: " + existing.title)
          result.addError(new ObjectError("newWebsite",
            "Found existing website with same name"))
          Future.successful(renderNewWebsiteForm(newWebsite))
        }
      }

      Await.result(eventualModelAndView, TenSeconds)
    }
  }

  private def renderNewWebsiteForm(newWebsite: nz.co.searchwellington.forms.NewWebsite): ModelAndView = {
    new ModelAndView("newWebsite").
      addObject("heading", "Adding a website").
      addObject("formObject", newWebsite).
      addObject("tags", Await.result(tagDAO.getAllTags, TenSeconds).asJava)
  }

}