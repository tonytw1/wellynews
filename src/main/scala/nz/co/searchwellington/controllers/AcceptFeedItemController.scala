package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.{FeedReaderUpdateService, RssfeedNewsitemService}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import nz.co.searchwellington.views.Errors
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Controller
class AcceptFeedItemController @Autowired()(mongoRepository: MongoRepository,
                                            urlBuilder: UrlBuilder,
                                            rssfeedNewsitemService: RssfeedNewsitemService,
                                            feedReaderUpdateService: FeedReaderUpdateService,
                                            loggedInUserFilter: LoggedInUserFilter) extends ReasonableWaits with AcceptancePolicyOptions with Errors {

  private val log = Logger.getLogger(classOf[AcceptFeedItemController])

  @RequestMapping(value = Array("/accept-feed-item"), method = Array(RequestMethod.GET))
  def accept(feed: String, url: String): ModelAndView = {

    val eventualModelAndView = Option(loggedInUserFilter.getLoggedInUser).map { loggedInUser =>
      mongoRepository.getFeedByUrlwords(feed).flatMap { fo =>
        fo.map { feed =>
          val eventualFeedItem = rssfeedNewsitemService.getFeedItemsAndDetailsFor(feed).map { fis =>
            fis.fold({ l =>
              log.warn("Could not read feed items: " + l)
              None
            }, { r =>
              r._1.find(fi => fi.url == url)
            })
          }

          eventualFeedItem.flatMap { maybeFeedItem =>
            maybeFeedItem.map { feedItemToAccept =>
              feedReaderUpdateService.acceptNewsitem(loggedInUser, feedItemToAccept, feed).map { accepted =>
                log.info("Accepted newsitem: " + accepted.title)
                Future.successful(new ModelAndView(new RedirectView(urlBuilder.getFeedUrl(feed))))
              }

            }.getOrElse {
              Future.successful(NotFound)
            }
          }

        }.getOrElse {
          Future.successful(NotFound)
        }
      }

    }.getOrElse{
      Future.successful(new ModelAndView()) // TODO not logged in
    }

    Await.result(eventualModelAndView, TenSeconds)
  }

}