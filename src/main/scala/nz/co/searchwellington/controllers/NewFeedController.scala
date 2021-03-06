package nz.co.searchwellington.controllers

import javax.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.WhakaokoService
import nz.co.searchwellington.forms.NewFeed
import nz.co.searchwellington.model.{Feed, Resource, UrlWordsGenerator, User}
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

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

@Controller
class NewFeedController @Autowired()(contentUpdateService: ContentUpdateService,
                                     mongoRepository: MongoRepository,
                                     urlWordsGenerator: UrlWordsGenerator, urlBuilder: UrlBuilder,
                                     whakaokoService: WhakaokoService,
                                     loggedInUserFilter: LoggedInUserFilter) extends ReasonableWaits with AcceptancePolicyOptions {

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
  def submit(@Valid @ModelAttribute("newFeed") newFeed: NewFeed, result: BindingResult): ModelAndView = {

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

        val owner = loggedInUserFilter.getLoggedInUser

        val f = Feed(title = Some(newFeed.getTitle),
          page = newFeed.getUrl,
          publisher = publisher.map(_._id),
          acceptance = newFeed.getAcceptancePolicy,
          owner = owner.map(_._id),
          date = Some(DateTime.now.toDate),
          held = submissionShouldBeHeld(owner)
        )
        val feed = f.copy(url_words = urlWordsGenerator.makeUrlWordsFor(f, publisher))

        val existingFeedWithSameUrlWords = Await.result(mongoRepository.getFeedByUrlwords(feed.url_words.get), TenSeconds)  // TODO naked get
        if (existingFeedWithSameUrlWords.nonEmpty) {
          result.addError(new ObjectError("urlWords", "Found existing feed with same URL words"))
        }

        if (!result.hasErrors) {

          val eventuallyCreated = whakaokoService.createFeedSubscription(feed.page).map { maybeSubscription =>
            feed.whakaokoSubscription = maybeSubscription
            feed
          }.flatMap { withWhakaokoSubscription =>
            contentUpdateService.create(withWhakaokoSubscription)
          }

          val created = Await.result(eventuallyCreated, TenSeconds)
          log.info("Created feed: " + created)

          new ModelAndView(new RedirectView(urlBuilder.getFeedUrl(feed)))

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

  // TODO duplication
  private def submissionShouldBeHeld(owner: Option[User]): Boolean = {
    !owner.exists(_.isAdmin)
  }

}