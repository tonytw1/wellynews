package nz.co.searchwellington.controllers

import javax.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.forms.NewWebsite
import nz.co.searchwellington.model.{UrlWordsGenerator, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.{ModelAttribute, RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import org.joda.time.DateTime

@Controller
class NewWebsiteController @Autowired()(contentUpdateService: ContentUpdateService,
                                        mongoRepository: MongoRepository,
                                        urlWordsGenerator: UrlWordsGenerator, urlBuilder: UrlBuilder,
                                        loggedInUserFilter: LoggedInUserFilter) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[NewWebsiteController])

  @RequestMapping(value = Array("/new-website"), method = Array(RequestMethod.GET))
  def prompt(): ModelAndView = {
    val mv = new ModelAndView("newWebsite")
    mv.addObject("newWebsite", new NewWebsite())
    return mv
  }

  @RequestMapping(value = Array("/new-website"), method = Array(RequestMethod.POST))
  def submit(@Valid @ModelAttribute("newWebsite") newWebsite: NewWebsite, result: BindingResult): ModelAndView = {

    if (result.hasErrors) {
      log.warn("New website submission has errors: " + result)
      val mv = new ModelAndView("newWebsite")
      mv.addObject("newWebsite", newWebsite)
      return mv

    } else {
      log.info("Got valid new website submission: " + newWebsite)

      val owner = Option(loggedInUserFilter.getLoggedInUser)

      val website = Website(title = Some(newWebsite.getTitle),
        page = Some(newWebsite.getUrl),
        url_words = Some(urlWordsGenerator.makeUrlWordsFromName(newWebsite.getTitle)),
        owner = owner.map(_._id),
        date = Some(DateTime.now.toDate)
      )

      contentUpdateService.create(website)
      log.info("Created website: " + website)

      new ModelAndView(new RedirectView(urlBuilder.getPublisherUrl(website.title.get)))
    }
  }

}