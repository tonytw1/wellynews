package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.reading.WhakaokoService
import nz.co.searchwellington.feeds.{FeedReaderUpdateService, RssfeedNewsitemService}
import nz.co.searchwellington.model.UrlWordsGenerator
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Controller
class AcceptFeedItemController @Autowired()(contentUpdateService: ContentUpdateService,
                                            mongoRepository: MongoRepository,
                                            urlWordsGenerator: UrlWordsGenerator, urlBuilder: UrlBuilder,
                                            whakaokoService: WhakaokoService,
                                            rssfeedNewsitemService: RssfeedNewsitemService,
                                            feedReaderUpdateService: FeedReaderUpdateService,
                                            loggedInUserFilter: LoggedInUserFilter) extends ReasonableWaits with AcceptancePolicyOptions {

  private val log = Logger.getLogger(classOf[AcceptFeedItemController])

  @RequestMapping(value = Array("/accept-feed-item"), method = Array(RequestMethod.GET))
  def accept(feed: String, url: String): ModelAndView = {

    val loggedInUser = loggedInUserFilter.getLoggedInUser // TODO map

    val eventualModelAndView = mongoRepository.getFeedByUrlwords(feed).flatMap { fo =>
      fo.map { feed =>
        rssfeedNewsitemService.getFeedItemsAndDetailsFor(feed).map { fis =>
          fis.fold({ l =>
            log.warn("Could not read feed items: " + l)
            new ModelAndView()

          }, { r =>
            r._1.find(fi => fi.url == url).map { feedItemToAccept =>
              feedReaderUpdateService.acceptNewsitem(loggedInUser, feedItemToAccept, feed)
            }
            new ModelAndView()  // TODO map
          })
        }

      }.getOrElse {
        Future.successful(new ModelAndView()) // TODO file not found
      }

    }

    Await.result(eventualModelAndView, TenSeconds)
  }

}