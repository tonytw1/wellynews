package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.forms.NewWatchlist
import nz.co.searchwellington.model.{UrlWordsGenerator, Watchlist}
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
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller
class NewWatchlistController @Autowired()(contentUpdateService: ContentUpdateService,
                                          mongoRepository: MongoRepository,
                                          urlWordsGenerator: UrlWordsGenerator, urlBuilder: UrlBuilder,
                                          val anonUserService: AnonUserService) extends ReasonableWaits
                                          with EnsuredSubmitter {

  private val log = Logger.getLogger(classOf[NewWatchlistController])

  @RequestMapping(value = Array("/new-watchlist"), method = Array(RequestMethod.GET))
  def prompt(): ModelAndView = {
    new ModelAndView("newWatchlist").addObject("newWatchlist", new NewWatchlist())
  }

  @RequestMapping(value = Array("/new-watchlist"), method = Array(RequestMethod.POST))
  def submit(@Valid @ModelAttribute("newWatchlist") newWatchlist: NewWatchlist, result: BindingResult, request: HttpServletRequest): ModelAndView = {
    val loggedInUser = getLoggedInUser(request)
    if (result.hasErrors) {
      log.warn("New website submission has errors: " + result)
      renderNewWatchlistForm(newWatchlist)

    } else {
      log.info("Got valid new watchlist submission: " + newWatchlist)
      val maybePublisher = trimToOption(newWatchlist.getPublisher).flatMap { publisherName =>
        Await.result(mongoRepository.getWebsiteByName(publisherName), TenSeconds)
      }

      val w = Watchlist(title = Some(newWatchlist.getTitle),
        page = newWatchlist.getUrl,
        date = Some(DateTime.now.toDate),
        publisher = maybePublisher.map(_._id)
      )
      val watchlist = w.copy(url_words = urlWordsGenerator.makeUrlWordsFor(w))

      Await.result(mongoRepository.getWebsiteByUrlwords(watchlist.url_words.get), TenSeconds).fold {  // TODO naked get
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
        renderNewWatchlistForm(newWatchlist)
      }
    }
  }

  private def renderNewWatchlistForm(newWatchlist: nz.co.searchwellington.forms.NewWatchlist): ModelAndView = {
    new ModelAndView("newWatchlist").
      addObject("heading", "Adding a watchlist item").
      addObject("newWatchlist", newWatchlist)
  }

}