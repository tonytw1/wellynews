package nz.co.searchwellington.controllers

import javax.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.forms.NewWebsite
import nz.co.searchwellington.model.{UrlWordsGenerator, User, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.{BindingResult, ObjectError}
import org.springframework.web.bind.annotation.{ModelAttribute, RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import org.joda.time.DateTime

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller
class NewWebsiteController @Autowired()(contentUpdateService: ContentUpdateService,
                                        mongoRepository: MongoRepository,
                                        urlWordsGenerator: UrlWordsGenerator, urlBuilder: UrlBuilder,
                                        loggedInUserFilter: LoggedInUserFilter) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[NewWebsiteController])

  @RequestMapping(value = Array("/new-website"), method = Array(RequestMethod.GET))
  def prompt(): ModelAndView = {
    new ModelAndView("newWebsite").addObject("newWebsite", new NewWebsite())
  }

  @RequestMapping(value = Array("/new-website"), method = Array(RequestMethod.POST))
  def submit(@Valid @ModelAttribute("newWebsite") newWebsite: NewWebsite, result: BindingResult): ModelAndView = {
    if (result.hasErrors) {
      log.warn("New website submission has errors: " + result)
      renderNewWebsiteForm(newWebsite)

    } else {
      log.info("Got valid new website submission: " + newWebsite)

      val proposedUrlWords = urlWordsGenerator.makeUrlWordsFromName(newWebsite.getTitle)

      Await.result(mongoRepository.getWebsiteByUrlwords(proposedUrlWords), TenSeconds).fold {
        val owner = Option(loggedInUserFilter.getLoggedInUser)

        val website = Website(title = Some(newWebsite.getTitle),
          page = Some(newWebsite.getUrl),
          url_words = Some(proposedUrlWords),
          owner = owner.map(_._id),
          date = Some(DateTime.now.toDate),
          held = submissionShouldBeHeld(owner),
        )

        contentUpdateService.create(website)
        log.info("Created website: " + website)
        new ModelAndView(new RedirectView(urlBuilder.getPublisherUrl(website.title.get)))

      } { existing =>
        log.warn("Found existing website site same url words: " + existing.title)
        result.addError(new ObjectError("newWebsite",
          "Found existing website with same name"))
        renderNewWebsiteForm(newWebsite)
      }
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

}