package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.forms.NewWebsite
import nz.co.searchwellington.model.{UrlWordsGenerator, User, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.{BindingResult, ObjectError}
import org.springframework.web.bind.annotation.{ModelAttribute, RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import javax.servlet.http.HttpServletRequest
import javax.validation.Valid
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Controller
class NewWebsiteController @Autowired()(contentUpdateService: ContentUpdateService,
                                        mongoRepository: MongoRepository,
                                        urlWordsGenerator: UrlWordsGenerator, urlBuilder: UrlBuilder,
                                        anonUserService: AnonUserService) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[NewWebsiteController])

  @RequestMapping(value = Array("/new-website"), method = Array(RequestMethod.GET))
  def prompt(): ModelAndView = {
    new ModelAndView("newWebsite").addObject("newWebsite", new NewWebsite())
  }

  @RequestMapping(value = Array("/new-website"), method = Array(RequestMethod.POST))
  def submit(@Valid @ModelAttribute("newWebsite") newWebsite: NewWebsite, result: BindingResult, request: HttpServletRequest): ModelAndView = {
    val loggedInUser = getLoggedInUser(request)

    if (result.hasErrors) {
      log.warn("New website submission has errors: " + result)
      renderNewWebsiteForm(newWebsite)

    } else {
      log.info("Got valid new website submission: " + newWebsite)
      val w = Website(title = Some(newWebsite.getTitle),
        page = newWebsite.getUrl,
        date = Some(DateTime.now.toDate),
      )
      val website = w.copy(url_words = urlWordsGenerator.makeUrlWordsFor(w))

      val eventualModelAndView = mongoRepository.getWebsiteByUrlwords(website.url_words.get).flatMap { maybeExistingWebsite =>
        maybeExistingWebsite.fold {
          val submittingUser = ensuredSubmittingUser(loggedInUser)
          val withSubmittingUser = website.copy(owner = Some(submittingUser._id), held = submissionShouldBeHeld(Some(submittingUser))))
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

  private def submissionShouldBeHeld(owner: Option[User]) = {
    !owner.exists(_.isAdmin)
  }

  private def renderNewWebsiteForm(newWebsite: nz.co.searchwellington.forms.NewWebsite): ModelAndView = {
    new ModelAndView("newWebsite").
      addObject("heading", "Adding a website").
      addObject("newWebsite", newWebsite)
  }

  def ensuredSubmittingUser(loggedInUser: Option[User]): User = {
    def createAnonUser: User = {
      log.info("Creating new anon user for resource submission")
      anonUserService.createAnonUser
    }
    loggedInUser.getOrElse(createAnonUser)
  }

  // TODO Spike In a Scala  / functional code base replacing the Spring request scoped loggedInUserFilter with
  // a trait and injecting the request into all calling controllers might be a better fit
  def getLoggedInUser(request: HttpServletRequest): Option[User] = {
    val sessionUser = request.getSession.getAttribute("user").asInstanceOf[User]
    Option(sessionUser)
  }

  private def setSignedInUser(request: HttpServletRequest, user: User) = {
    log.info("Setting signed in user: " + user)
    request.getSession.setAttribute("user", user)
  }

}