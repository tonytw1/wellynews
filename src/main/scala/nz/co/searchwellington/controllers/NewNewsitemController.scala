package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.submission.EndUserInputs
import nz.co.searchwellington.forms.NewNewsitem
import nz.co.searchwellington.model.{Newsitem, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.{UrlBuilder, UrlCleaner}
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.{ModelAttribute, RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import javax.servlet.http.HttpServletRequest
import javax.validation.Valid
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

@Controller
class NewNewsitemController @Autowired()(contentUpdateService: ContentUpdateService,
                                         mongoRepository: MongoRepository, urlBuilder: UrlBuilder,
                                         val anonUserService: AnonUserService,
                                         val urlCleaner: UrlCleaner) extends ReasonableWaits
                                         with EnsuredSubmitter with EndUserInputs {

  private val log = Logger.getLogger(classOf[NewNewsitemController])
  private val dateFormatter = ISODateTimeFormat.basicDate()

  @RequestMapping(value = Array("/new-newsitem"), method = Array(RequestMethod.GET))
  def prompt(): ModelAndView = {
    val newNewsitem = new NewNewsitem()
    newNewsitem.setDate(dateFormatter.print(DateTime.now()))
    renderNewNewsitemForm(newNewsitem)
  }

  @RequestMapping(value = Array("/new-newsitem"), method = Array(RequestMethod.POST))
  def submit(@Valid @ModelAttribute("formObject") newNewsitem: NewNewsitem, result: BindingResult, request: HttpServletRequest): ModelAndView = {
    val loggedInUser = getLoggedInUser(request)
    if (result.hasErrors) {
      log.warn("New newsitem submission has errors: " + result)
      renderNewNewsitemForm(newNewsitem)

    } else {
      log.info("Got valid new newsitem submission: " + newNewsitem)
      val parsedDate = dateFormatter.parseDateTime(newNewsitem.getDate)

      val eventualMaybePublisher = trimToOption(newNewsitem.getPublisher).map { publisherName =>
        mongoRepository.getWebsiteByName(publisherName)
      }.getOrElse {
        Future.successful(None)
      }

      val eventualModelAndView = for {
        maybePublisher <- eventualMaybePublisher
        mv <- {
          val newsitem = Newsitem(
            title = Some(processTitle(newNewsitem.getTitle)),
            page = cleanUrl(newNewsitem.getUrl),
            date = Some(parsedDate.toDate),
            publisher = maybePublisher.map(_._id),
            description = Some(newNewsitem.getDescription.trim)
          )

          val submittingUser = ensuredSubmittingUser(loggedInUser)
          val withSubmittingUser = newsitem.copy(owner = Some(submittingUser._id), held = submissionShouldBeHeld(Some(submittingUser)))

          val eventualResource = contentUpdateService.create(withSubmittingUser)
          eventualResource.map { updated =>
            log.info("Created newsitem: " + updated)
            setSignedInUser(request, submittingUser)
            exitFromNewsitemSubmit(withSubmittingUser, maybePublisher)
          }
        }
      } yield {
        mv
      }

      Await.result(eventualModelAndView, TenSeconds)
    }
  }

  private def renderNewNewsitemForm(newNewsitem: nz.co.searchwellington.forms.NewNewsitem): ModelAndView = {
    new ModelAndView("newNewsitem").
      addObject("heading", "Adding a newsitem").
      addObject("formObject", newNewsitem)
  }

  private def exitFromNewsitemSubmit(newsitem: Newsitem, maybePublisher: Option[Website]): ModelAndView = {
    val redirection = maybePublisher.map { p =>
      new RedirectView(urlBuilder.getPublisherUrl(p))
    }.getOrElse {
      new RedirectView(urlBuilder.getLocalPageUrl(newsitem))
    }
    new ModelAndView(redirection)
  }

}
