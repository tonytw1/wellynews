package nz.co.searchwellington.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.submission.{EndUserInputs, GeotagParsing}
import nz.co.searchwellington.forms.NewNewsitem
import nz.co.searchwellington.geocoding.osm.GeoCodeService
import nz.co.searchwellington.model.{Newsitem, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.{UrlBuilder, UrlCleaner}
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.{GetMapping, ModelAttribute, PostMapping}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Controller
class NewNewsitemController @Autowired()(contentUpdateService: ContentUpdateService,
                                         mongoRepository: MongoRepository, urlBuilder: UrlBuilder,
                                         val anonUserService: AnonUserService,
                                         val urlCleaner: UrlCleaner,
                                         val geocodeService: GeoCodeService,
                                         loggedInUserFilter: LoggedInUserFilter) extends ReasonableWaits
                                         with EnsuredSubmitter with EndUserInputs with GeotagParsing {

  private val log = LogFactory.getLog(classOf[NewNewsitemController])
  private val dateFormatter = ISODateTimeFormat.basicDate()

  @GetMapping(Array("/new-newsitem"))
  def prompt(): ModelAndView = {
    val newNewsitem = new NewNewsitem()
    newNewsitem.setDate(dateFormatter.print(DateTime.now()))
    renderNewNewsitemForm(newNewsitem)
  }

  @PostMapping(Array("/new-newsitem"))
  def submit(@Valid @ModelAttribute("formObject") formObject: NewNewsitem, result: BindingResult, request: HttpServletRequest): ModelAndView = {
    val loggedInUser = loggedInUserFilter.getLoggedInUser
    if (result.hasErrors) {
      log.warn("New newsitem submission has errors: " + result)
      renderNewNewsitemForm(formObject)

    } else {
      log.info("Got valid new newsitem submission: " + formObject)
      val url = cleanUrl(formObject.getUrl).toOption.get.toExternalForm  // TODO error handling
      val parsedDate = dateFormatter.parseDateTime(formObject.getDate)

      val eventualMaybePublisher = trimToOption(formObject.getPublisher).map { publisherName =>
        mongoRepository.getWebsiteByName(publisherName)
      }.getOrElse {
        Future.successful(None)
      }

      val eventualModelAndView = for {
        maybePublisher <- eventualMaybePublisher
        mv <- {

          val geocode = Option(formObject.getGeocode).flatMap { address =>
            Option(formObject.getOsm).flatMap { osmId =>
              parseGeotag(address, osmId)
            }
          }

          val newsitem = Newsitem(
            title = processTitle(formObject.getTitle),
            page = url,
            date = Some(parsedDate.toDate),
            publisher = maybePublisher.map(_._id),
            description = Some(formObject.getDescription.trim),
            geocode = geocode
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
