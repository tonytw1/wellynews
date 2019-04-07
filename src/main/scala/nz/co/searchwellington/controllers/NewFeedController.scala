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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.{ModelAttribute, RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.Await

@Controller
class NewFeedController @Autowired()(contentUpdateService: ContentUpdateService,
                                     mongoRepository: MongoRepository,
                                     urlWordsGenerator: UrlWordsGenerator, urlBuilder: UrlBuilder,
                                     whakaokoService: WhakaokoService,
                                     loggedInUserFilter: LoggedInUserFilter) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[NewFeedController])

  @RequestMapping(value = Array("/new-feed"), method = Array(RequestMethod.GET))
  def prompt(): ModelAndView = {
    val mv = new ModelAndView("newFeed")
    mv.addObject("newFeed", new NewFeed())
    return mv
  }

  @RequestMapping(value = Array("/new-feed"), method = Array(RequestMethod.POST))
  def submit(@Valid @ModelAttribute("newFeed") newFeed: NewFeed, result: BindingResult): ModelAndView = {

    if (result.hasErrors) {
      log.warn("New feed submission has errors: " + result)
      val mv = new ModelAndView("newFeed")
      mv.addObject("newFeed", newFeed)
      return mv

    } else {
      log.info("Got valid new feed submission: " + newFeed)


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
        url_words = Some(urlWordsGenerator.makeUrlWordsFromName(newFeed.getTitle)),
        publisher = publisher.map(_._id),
        acceptance =  newFeed.getAcceptancePolicy,
        owner = owner.map(_._id)
      )

      contentUpdateService.create(feed)
      log.info("Created feed: " + feed)

      feed.page.map { feedUrl =>
        whakaokoService.createFeedSubscription(feedUrl)
      }

      new ModelAndView(new RedirectView(urlBuilder.getFeedUrl(feed)))
    }
  }

}