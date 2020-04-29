package nz.co.searchwellington.controllers

import javax.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.forms.NewNewsitem
import nz.co.searchwellington.model.{Newsitem, User, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.{ModelAttribute, RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller
class NewNewsitemController @Autowired()(contentUpdateService: ContentUpdateService,
                                         loggedInUserFilter: LoggedInUserFilter,
                                         mongoRepository: MongoRepository, urlBuilder: UrlBuilder) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[NewNewsitemController])
  private val dateFormatter = ISODateTimeFormat.basicDate()

  @RequestMapping(value = Array("/new-newsitem"), method = Array(RequestMethod.GET))
  def prompt(): ModelAndView = {
    val newsitem = new NewNewsitem()
    newsitem.setDate(dateFormatter.print(DateTime.now()))
    new ModelAndView("newNewsitem").addObject("newNewsitem", newsitem)
  }

  @RequestMapping(value = Array("/new-newsitem"), method = Array(RequestMethod.POST))
  def submit(@Valid @ModelAttribute("newNewsitem") newNewsitem: NewNewsitem, result: BindingResult): ModelAndView = {
    if (result.hasErrors) {
      log.warn("New newsitem submission has errors: " + result)
      renderNewNewsitemForm(newNewsitem)

    } else {
      log.info("Got valid new newsitem submission: " + newNewsitem)
      val owner = loggedInUserFilter.getLoggedInUser

      val parsedDate = dateFormatter.parseDateTime(newNewsitem.getDate)

      val publisherName = if (newNewsitem.getPublisher.trim.nonEmpty) {
        Some(newNewsitem.getPublisher.trim)
      } else {
        None
      }
      val publisher: Option[Website] = publisherName.flatMap { publisherName =>
        Await.result(mongoRepository.getWebsiteByName(publisherName), TenSeconds)
      }

      val newsitem = Newsitem(
        title = Some(newNewsitem.getTitle),
        page = Some(newNewsitem.getUrl),
        owner = owner.map(_._id),
        date = Some(parsedDate.toDate),
        held = submissionShouldBeHeld(owner),
        publisher = publisher.map(_._id),
        description = Some(newNewsitem.getDescription.trim)
      )

      contentUpdateService.create(newsitem)
      log.info("Created newsitem: " + newsitem)

      val redirection = publisher.map { p =>
        new RedirectView(urlBuilder.getPublisherUrl(p))
      }.getOrElse{
        new RedirectView("/TODO")
      }
      new ModelAndView(redirection)
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
