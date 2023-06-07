package nz.co.searchwellington.controllers

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.submission.EndUserInputs
import nz.co.searchwellington.feeds.whakaoko.WhakaokoService
import nz.co.searchwellington.forms.NewFeed
import nz.co.searchwellington.model.{Feed, UrlWordsGenerator, User}
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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

@Controller
class NewFeedController @Autowired()(contentUpdateService: ContentUpdateService,
                                     mongoRepository: MongoRepository,
                                     urlWordsGenerator: UrlWordsGenerator,
                                     urlBuilder: UrlBuilder,
                                     whakaokoService: WhakaokoService,
                                     val anonUserService: AnonUserService,
                                     val urlCleaner: UrlCleaner,
                                     val contentRetrievalService: ContentRetrievalService,
                                     val tagDAO: TagDAO,
                                     loggedInUserFilter: LoggedInUserFilter) extends EditScreen
  with ReasonableWaits with EnsuredSubmitter with AcceptancePolicyOptions with EndUserInputs {

  private val log = LogFactory.getLog(classOf[NewFeedController])

  @GetMapping(Array("/new-feed"))
  def prompt(publisher: String): ModelAndView = {
    implicit val currentSpan: Span = Span.current()

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
      withPublisherPrepopulated.setTitle(p.title + " feed")
      withPublisherPrepopulated.setPublisher(p.title)
      withPublisherPrepopulated
    }

    renderForm(newFeedForm, loggedInUserFilter.getLoggedInUser)
  }

  @PostMapping(Array("/new-feed"))
  def submit(@Valid @ModelAttribute("formObject") newFeed: NewFeed, result: BindingResult, request: HttpServletRequest): ModelAndView = {
    implicit val currentSpan: Span = Span.current()
    val loggedInUser = loggedInUserFilter.getLoggedInUser
    if (!result.hasErrors) {
      log.info("Got valid new feed submission: " + newFeed)

      if (!result.hasErrors) {
        val url = cleanUrl(newFeed.getUrl).toOption.get.toExternalForm // TODO error handling


        val eventualMaybePublisher = trimToOption(newFeed.getPublisher).map { publisherName =>
          mongoRepository.getWebsiteByName(publisherName)
        }.getOrElse {
          Future.successful(None)
        }
        val maybePublisher = Await.result(eventualMaybePublisher, TenSeconds)

        val f = Feed(title = processTitle(newFeed.getTitle),
          page = url,
          publisher = maybePublisher.map(_._id),
          acceptance = newFeed.getAcceptancePolicy,
          date = Some(DateTime.now.toDate),
        )

        val urlWords = urlWordsGenerator.makeUrlWordsFor(f, maybePublisher)
        val feed = f.copy(url_words = Some(urlWords))
        val eventualMaybeExistingFeed = mongoRepository.getFeedByUrlwords(urlWords)

        val existingFeedWithSameUrlWords = Await.result(eventualMaybeExistingFeed, TenSeconds)
        if (existingFeedWithSameUrlWords.nonEmpty) {
          result.addError(new ObjectError("urlWords", "Found existing feed with same URL words"))
        }

        if (!result.hasErrors) {
          val submittingUser = ensuredSubmittingUser(loggedInUser)
          val withSubmittingUser = feed.copy(owner = Some(submittingUser._id), held = submissionShouldBeHeld(Some(submittingUser)))

          val eventuallyMaybeCreated = whakaokoService.createFeedSubscription(withSubmittingUser.page).map { maybeSubscription =>
            withSubmittingUser.whakaokoSubscription = maybeSubscription
            withSubmittingUser
          }.flatMap { feed =>
            contentUpdateService.create(feed).map { r =>
              if (r) {
                Some(feed)
              } else {
                None
              }
            }
          }

          val eventualResult = eventuallyMaybeCreated.flatMap { maybeCreated =>
            maybeCreated.map { created =>
              log.info("Created feed: " + maybeCreated)
              mongoRepository.deleteDiscoveredFeed(created.page).map { r =>
                r.writeErrors.isEmpty
              }
            }.getOrElse {
              log.warn("Failed to create feed: " + maybeCreated)
              Future.successful(false)
            }
          }

          Await.result(eventualResult, TenSeconds)

          setSignedInUser(request, submittingUser)
          new ModelAndView(new RedirectView(urlBuilder.getFeedUrl(withSubmittingUser)))

        } else {
          log.warn("New feed submission has errors: " + result)
          renderForm(newFeed, loggedInUser)
        }

      } else {
        log.warn("New feed submission has errors: " + result)
        renderForm(newFeed, loggedInUser)
      }

    } else {
      log.warn("New feed submission has errors: " + result)
      renderForm(newFeed, loggedInUser)
    }
  }

  private def renderForm(newFeed: NewFeed, loggedInUser: Option[User])(implicit currentSpan: Span): ModelAndView = {
   editScreen("newFeed","Adding a feed", loggedInUser).
      addObject("acceptancePolicyOptions", acceptancePolicyOptions.asJava).
      addObject("formObject", newFeed)
  }

}