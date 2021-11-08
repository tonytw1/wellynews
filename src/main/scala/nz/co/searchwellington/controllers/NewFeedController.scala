package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.submission.EndUserInputs
import nz.co.searchwellington.feeds.whakaoko.WhakaokoService
import nz.co.searchwellington.forms.NewFeed
import nz.co.searchwellington.model.{Feed, UrlWordsGenerator, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.{UrlBuilder, UrlCleaner}
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
class NewFeedController @Autowired()(contentUpdateService: ContentUpdateService,
                                     mongoRepository: MongoRepository,
                                     urlWordsGenerator: UrlWordsGenerator, urlBuilder: UrlBuilder,
                                     whakaokoService: WhakaokoService,
                                     val anonUserService: AnonUserService,
                                     val urlCleaner: UrlCleaner) extends ReasonableWaits with EnsuredSubmitter
  with AcceptancePolicyOptions with EndUserInputs {

  private val log = Logger.getLogger(classOf[NewFeedController])

  @RequestMapping(value = Array("/new-feed"), method = Array(RequestMethod.GET))
  def prompt(publisher: String): ModelAndView = {
    val prepopulatedPublisher = {
      Option(publisher).flatMap { p =>
        if (p.trim.nonEmpty) {
          Await.result(mongoRepository.getWebsiteByUrlwords(p), TenSeconds)
        } else {
          None
        }
      }
    }

    val newFeedForm = prepopulatedPublisher.fold {
      new NewFeed()
    } { p =>
      val withPublisherPrepopulated = new NewFeed()
      withPublisherPrepopulated.setTitle(p.title.map(t => t + " feed").getOrElse(""))
      withPublisherPrepopulated.setPublisher(p.title.getOrElse(""))
      withPublisherPrepopulated
    }

    renderForm(newFeedForm)
  }

  @RequestMapping(value = Array("/new-feed"), method = Array(RequestMethod.POST))
  def submit(@Valid @ModelAttribute("newFeed") newFeed: NewFeed, result: BindingResult, request: HttpServletRequest): ModelAndView = {
    val loggedInUser = getLoggedInUser(request)
    if (!result.hasErrors) {
      log.info("Got valid new feed submission: " + newFeed)

      if (!result.hasErrors) {
        val publisherName = if (newFeed.getPublisher.trim.nonEmpty) {
          Some(newFeed.getPublisher.trim)
        } else {
          None
        }
        val publisher = publisherName.flatMap { publisherName =>
          Await.result(mongoRepository.getWebsiteByName(publisherName), TenSeconds)
        }

        val f = Feed(title = Some(newFeed.getTitle),
          page = cleanUrl(newFeed.getUrl),
          publisher = publisher.map(_._id),
          acceptance = newFeed.getAcceptancePolicy,
          date = Some(DateTime.now.toDate),
        )
        val feed = f.copy(url_words = urlWordsGenerator.makeUrlWordsFor(f, publisher))

        val existingFeedWithSameUrlWords = Await.result(mongoRepository.getFeedByUrlwords(feed.url_words.get), TenSeconds)  // TODO naked get
        if (existingFeedWithSameUrlWords.nonEmpty) {
          result.addError(new ObjectError("urlWords", "Found existing feed with same URL words"))
        }

        if (!result.hasErrors) {
          val submittingUser = ensuredSubmittingUser(loggedInUser)
          val withSubmittingUser = feed.copy(owner = Some(submittingUser._id), held = submissionShouldBeHeld(Some(submittingUser)))

          val eventuallyCreated = whakaokoService.createFeedSubscription(withSubmittingUser.page).map { maybeSubscription =>
            withSubmittingUser.whakaokoSubscription = maybeSubscription
            withSubmittingUser
          }.flatMap { withWhakaokoSubscription =>
            contentUpdateService.create(withWhakaokoSubscription)
          }

          val created = Await.result(eventuallyCreated, TenSeconds)
          log.info("Created feed: " + created)

          Await.result(mongoRepository.deleteDiscoveredFeed(created.page), TenSeconds)

          setSignedInUser(request, submittingUser)
          new ModelAndView(new RedirectView(urlBuilder.getFeedUrl(withSubmittingUser)))

        } else {
          log.warn("New feed submission has errors: " + result)
          renderForm(newFeed)
        }

      } else {
        log.warn("New feed submission has errors: " + result)
        renderForm(newFeed)
      }

    } else {
      log.warn("New feed submission has errors: " + result)
      renderForm(newFeed)
    }
  }

  private def renderForm(newFeed: NewFeed) = {
    import scala.collection.JavaConverters._
    new ModelAndView("newFeed").
      addObject("heading", "Adding a feed").
      addObject("acceptancePolicyOptions", acceptancePolicyOptions.asJava).
      addObject("newFeed", newFeed)
  }

}