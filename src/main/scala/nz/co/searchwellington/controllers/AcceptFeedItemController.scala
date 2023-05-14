package nz.co.searchwellington.controllers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.FeedReaderUpdateService
import nz.co.searchwellington.feeds.whakaoko.WhakaokoFeedReader
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import nz.co.searchwellington.views.Errors
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Controller
class AcceptFeedItemController @Autowired()(mongoRepository: MongoRepository,
                                            urlBuilder: UrlBuilder,
                                            whakaokoFeedReader: WhakaokoFeedReader,
                                            feedReaderUpdateService: FeedReaderUpdateService,
                                            loggedInUserFilter: LoggedInUserFilter
                                           ) extends ReasonableWaits with AcceptancePolicyOptions with Errors {

  private val log = LogFactory.getLog(classOf[AcceptFeedItemController])

  @GetMapping(value = Array("/accept-feed-item"))
  def accept(feed: String, url: String): ModelAndView = {
    implicit val currentSpan: Span = Span.current()
    val eventualModelAndView = loggedInUserFilter.getLoggedInUser.map { loggedInUser =>
      mongoRepository.getFeedByUrlwords(feed).flatMap { fo =>
        fo.map { feed =>
          val eventualFeedItemToAccept = whakaokoFeedReader.fetchFeedItems(feed).map { fis =>
            fis.fold({ l =>
              log.warn("Could not read feed items: " + l)
              None
            }, { r =>
              r._1.find(fi => fi.url == url)
            })
          }

          eventualFeedItemToAccept.flatMap { maybeFeedItem =>
            maybeFeedItem.map { feedItemToAccept =>
              feedReaderUpdateService.acceptFeeditem(loggedInUser, feedItemToAccept, feed, feedItemToAccept.categories.getOrElse(Seq.empty)).map { maybeAccepted =>
                maybeAccepted.map { accepted =>
                  log.info("Accepted news item: " + accepted.title)
                  new ModelAndView(new RedirectView(urlBuilder.getFeedUrl(feed)))
                }.getOrElse {
                  log.warn("Feed item was not acceptable")
                  NotFound
                }

              } recover {
                case e: Exception =>
                  log.error("Error while accepting feed item", e)
                  NotFound
              }
            }.getOrElse {
              Future.successful(NotFound)
            }

          }
        }.getOrElse {
          Future.successful(NotFound)
        }
      }

    }.getOrElse {
      Future.successful(new ModelAndView()) // TODO redirect to login
    }

    Await.result(eventualModelAndView, TenSeconds)
  }

}