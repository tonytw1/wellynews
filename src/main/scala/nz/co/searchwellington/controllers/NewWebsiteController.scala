package nz.co.searchwellington.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.submission.EndUserInputs
import nz.co.searchwellington.forms.NewWebsite
import nz.co.searchwellington.model.{UrlWordsGenerator, User, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{HandTaggingService, TagDAO}
import nz.co.searchwellington.urls.{UrlBuilder, UrlCleaner}
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.{BindingResult, ObjectError}
import org.springframework.web.bind.annotation.{GetMapping, ModelAttribute, PostMapping}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

@Controller
class NewWebsiteController @Autowired()(contentUpdateService: ContentUpdateService,
                                        mongoRepository: MongoRepository,
                                        urlWordsGenerator: UrlWordsGenerator, urlBuilder: UrlBuilder,
                                        val anonUserService: AnonUserService,
                                        val urlCleaner: UrlCleaner,
                                        val tagDAO: TagDAO,
                                        handTaggingService: HandTaggingService,
                                        loggedInUserFilter: LoggedInUserFilter) extends EditScreen
  with ReasonableWaits with EnsuredSubmitter with EndUserInputs {

  private val log = LogFactory.getLog(classOf[NewWebsiteController])

  @GetMapping(Array("/new-website"))
  def prompt(): ModelAndView = renderNewWebsiteForm(new NewWebsite(), loggedInUserFilter.getLoggedInUser)

  @PostMapping(Array("/new-website"))
  def submit(@Valid @ModelAttribute("formObject") formObject: NewWebsite, result: BindingResult, request: HttpServletRequest): ModelAndView = {
    val loggedInUser = loggedInUserFilter.getLoggedInUser

    if (result.hasErrors) {
      log.warn("New website submission has errors: " + result)
      renderNewWebsiteForm(formObject, loggedInUser)

    } else {
      log.info("Got valid new website submission: " + formObject)
      val url = cleanUrl(formObject.getUrl).toOption.get.toExternalForm // TODO error handling
      val title = processTitle(formObject.getTitle)
      val urlWords = urlWordsGenerator.makeUrlWordsFromName(title)
      val submittedTags = Await.result(tagDAO.loadTagsById(formObject.getTags.asScala.toSeq), TenSeconds).toSet

      val eventualModelAndView = mongoRepository.getWebsiteByUrlwords(urlWords).flatMap { maybeExistingWebsite =>
        maybeExistingWebsite.fold {
          val submittingUser = ensuredSubmittingUser(loggedInUser)
          val withTags = handTaggingService.setUsersTagging(submittingUser, submittedTags.map(_._id),
            Website(title = title,
              page = url,
              date = Some(DateTime.now.toDate),
              url_words = Some(urlWords),
              owner = Some(submittingUser._id),
              held = submissionShouldBeHeld(Some(submittingUser)))
          ).asInstanceOf[Website]

          contentUpdateService.create(withTags).map { _ =>
            log.info("Created website: " + withTags)
            setSignedInUser(request, submittingUser)
            new ModelAndView(new RedirectView(urlBuilder.getPublisherUrl(withTags)))
          }

        } { existing =>
          log.warn("Found existing website site same url words: " + existing.title)
          result.addError(new ObjectError("newWebsite",
            "Found existing website with same name"))
          Future.successful(renderNewWebsiteForm(formObject, loggedInUser))
        }
      }

      Await.result(eventualModelAndView, TenSeconds)
    }
  }

  private def renderNewWebsiteForm(newWebsite: NewWebsite, loggedInUser: Option[User]): ModelAndView = {
   editScreen("newWebsite", "Adding a website", loggedInUser).
      addObject("formObject", newWebsite).
      addObject("tags", Await.result(tagDAO.getAllTags, TenSeconds).asJava)
  }

}