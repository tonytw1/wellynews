package nz.co.searchwellington.controllers

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.submission.EndUserInputs
import nz.co.searchwellington.forms.NewWatchlist
import nz.co.searchwellington.model.{UrlWordsGenerator, User, Watchlist}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.{ContentRetrievalService, TagDAO}
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

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller
class NewWatchlistController @Autowired()(contentUpdateService: ContentUpdateService,
                                          mongoRepository: MongoRepository,
                                          urlWordsGenerator: UrlWordsGenerator, urlBuilder: UrlBuilder,
                                          val anonUserService: AnonUserService,
                                          val urlCleaner: UrlCleaner,
                                          val contentRetrievalService: ContentRetrievalService,
                                          val tagDAO: TagDAO,
                                          loggedInUserFilter: LoggedInUserFilter) extends EditScreen with ReasonableWaits
                                          with EnsuredSubmitter with EndUserInputs {

  private val log = LogFactory.getLog(classOf[NewWatchlistController])

  @GetMapping(Array("/new-watchlist"))
  def prompt(): ModelAndView = {
    implicit val currentSpan: Span = Span.current()
    renderNewWatchlistForm(new NewWatchlist(), loggedInUserFilter.getLoggedInUser)
  }

  @PostMapping(Array("/new-watchlist"))
  def submit(@Valid @ModelAttribute("formObject") newWatchlist: NewWatchlist, result: BindingResult, request: HttpServletRequest): ModelAndView = {
    val loggedInUser = loggedInUserFilter.getLoggedInUser
    implicit val currentSpan: Span = Span.current()

    if (result.hasErrors) {
      log.warn("New website submission has errors: " + result)
      renderNewWatchlistForm(newWatchlist, loggedInUser)

    } else {
      log.info("Got valid new watchlist submission: " + newWatchlist)
      val url = cleanUrl(newWatchlist.getUrl).toOption.get.toExternalForm  // TODO error handling
      val maybePublisher = trimToOption(newWatchlist.getPublisher).flatMap { publisherName =>
        Await.result(mongoRepository.getWebsiteByName(publisherName), TenSeconds)
      }

      val w = Watchlist(title = processTitle(newWatchlist.getTitle),
        page = url,
        date = Some(DateTime.now.toDate),
        publisher = maybePublisher.map(_._id)
      )
      val urlWords = urlWordsGenerator.makeUrlWordsFor(w)

      Await.result(mongoRepository.getWebsiteByUrlwords(urlWords), TenSeconds).fold {
        val submittingUser = ensuredSubmittingUser(loggedInUser)
        val withSubmittingUser = w.copy(owner = Some(submittingUser._id), held = submissionShouldBeHeld(Some(submittingUser)))

        contentUpdateService.create(withSubmittingUser)
        log.info("Created watchlist: " + withSubmittingUser)
        setSignedInUser(request, submittingUser)
        new ModelAndView(new RedirectView(urlBuilder.getWatchlistUrl))

      } { existing => // TODO on url not url words
        log.warn("Found existing watchlist site same url words: " + existing.title)
        result.addError(new ObjectError("newWatchlist",
          "Found existing watchlist with same name"))
        renderNewWatchlistForm(newWatchlist, loggedInUser)
      }
    }
  }

  private def renderNewWatchlistForm(newWatchlist: NewWatchlist, loggedInUser: Option[User])(implicit currentSpan: Span): ModelAndView = {
    editScreen("newWatchlist", "Adding a watchlist item", loggedInUser).
      addObject("formObject", newWatchlist)
  }

}