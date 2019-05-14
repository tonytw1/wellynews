package nz.co.searchwellington.controllers

import javax.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.reading.WhakaokoService
import nz.co.searchwellington.forms.NewFeed
import nz.co.searchwellington.model.{Feed, UrlWordsGenerator}
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

import scala.concurrent.Await

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
      withPublisherPrepopulated.setPublisher(p.title.getOrElse(""))
      withPublisherPrepopulated
    }

    renderForm(newFeedForm)
  }

  @RequestMapping(value = Array("/new-feed"), method = Array(RequestMethod.POST))
  def submit(@Valid @ModelAttribute("newFeed") newFeed: NewFeed, result: BindingResult): ModelAndView = {

    if (!result.hasErrors) {
      log.info("Got valid new feed submission: " + newFeed)

      val urlWordsFromTitle = urlWordsGenerator.makeUrlWordsFromName(newFeed.getTitle)

      val existingFeedWithSameUrlWords = Await.result(mongoRepository.getFeedByUrlwords(urlWordsFromTitle), TenSeconds)
      if (existingFeedWithSameUrlWords.nonEmpty) {
        result.addError(new ObjectError("urlWords", "Found existing feed with same URL words"))
      }

      if (!result.hasErrors) {
        val publisherName = if (newFeed.getPublisher.trim.nonEmpty) {
          Some(newFeed.getPublisher.trim)
        } else {
          None
        }
        val publisher = publisherName.flatMap { publisherName =>
          Await.result(mongoRepository.getWebsiteByName(publisherName), TenSeconds)
        }

        val owner = Option(loggedInUserFilter.getLoggedInUser)

        val feed = Feed(title = Some(newFeed.getTitle),
          page = Some(newFeed.getUrl),
          url_words = Some(urlWordsFromTitle),
          publisher = publisher.map(_._id),
          acceptance = newFeed.getAcceptancePolicy,
          owner = owner.map(_._id),
          date = Some(DateTime.now.toDate)
        )

        contentUpdateService.create(feed)
        log.info("Created feed: " + feed)

        feed.page.map { feedUrl =>
          whakaokoService.createFeedSubscription(feedUrl)
        }

        new ModelAndView(new RedirectView(urlBuilder.getFeedUrl(feed)))

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
    val mv = new ModelAndView("newFeed")
    mv.addObject("Add a new feed")
    import scala.collection.JavaConverters._
    mv.addObject("acceptancePolicyOptions", acceptancePolicyOptions.asJava)
    mv.addObject("newFeed", newFeed)
    mv
  }

}