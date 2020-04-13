package nz.co.searchwellington.controllers

import javax.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.forms.NewNewsitem
import nz.co.searchwellington.model.{Newsitem, User}
import nz.co.searchwellington.modification.ContentUpdateService
import org.apache.log4j.Logger
import org.joda.time.format.ISODateTimeFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.{ModelAttribute, RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.ExecutionContext.Implicits.global

@Controller
class NewNewsitemController @Autowired()(contentUpdateService: ContentUpdateService,
                                         loggedInUserFilter: LoggedInUserFilter) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[NewNewsitemController])

  @RequestMapping(value = Array("/new-newsitem"), method = Array(RequestMethod.GET))
  def prompt(): ModelAndView = {
    new ModelAndView("newNewsitem").addObject("newNewsitem", new NewNewsitem())
  }

  @RequestMapping(value = Array("/new-newsitem"), method = Array(RequestMethod.POST))
  def submit(@Valid @ModelAttribute("newNewsitem") newNewsitem: NewNewsitem, result: BindingResult): ModelAndView = {
    if (result.hasErrors) {
      log.warn("New newsitem submission has errors: " + result)
      renderNewNewsitemForm(newNewsitem)

    } else {
      log.info("Got valid new newsitem submission: " + newNewsitem)


      val owner = Option(loggedInUserFilter.getLoggedInUser)

      val dateFormatter = ISODateTimeFormat.basicDate()
      val parsedDate = dateFormatter.parseDateTime(newNewsitem.getDate)

      val newsitem = Newsitem(
        title = Some(newNewsitem.getTitle),
        page = Some(newNewsitem.getUrl),
        owner = owner.map(_._id),
        date = Some(parsedDate.toDate),
        held = submissionShouldBeHeld(owner),
      )

      contentUpdateService.create(newsitem)
      log.info("Created newsitem: " + newsitem)
      new ModelAndView(new RedirectView("/TODO"))
    }
  }

  private def submissionShouldBeHeld(owner: Option[User]): Boolean = {
    !owner.exists(_.isAdmin)
  }

  private def renderNewNewsitemForm(newNewsitem: nz.co.searchwellington.forms.NewNewsitem): ModelAndView = {
    new ModelAndView("newNewsitem").
      addObject("heading", "Adding a newsitem").
      addObject("newNewsitem", newNewsitem)
  }

}