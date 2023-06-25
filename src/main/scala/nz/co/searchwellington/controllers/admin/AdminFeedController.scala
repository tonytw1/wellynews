package nz.co.searchwellington.controllers.admin

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RequiringLoggedInUser}
import nz.co.searchwellington.feeds.reading.ReadFeedRequest
import nz.co.searchwellington.model.{FeedAcceptancePolicy, User}
import nz.co.searchwellington.permissions.EditPermissionService
import nz.co.searchwellington.queues.ReadFeedQueue
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import nz.co.searchwellington.views.Errors
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{GetMapping, PathVariable}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Order(6)
@Controller class AdminFeedController @Autowired()(readFeedQueue: ReadFeedQueue,
                                                   urlBuilder: UrlBuilder,
                                                   editPermissionService: EditPermissionService,
                                                   mongoRepository: MongoRepository,
                                                   val loggedInUserFilter: LoggedInUserFilter)
  extends RequiringLoggedInUser with Errors with ReasonableWaits {

  @GetMapping(Array("/feed/{urlWords}/accept-all"))
  def acceptAllFrom(@PathVariable urlWords: String): ModelAndView = {
    implicit val currentSpan: Span = Span.current()

    def accept(loggedInUser: User): ModelAndView = {
      val eventualResult = mongoRepository.getFeedByUrlwords(urlWords).map { maybeFeed =>
        maybeFeed.map { feed =>
          if (editPermissionService.canAcceptAllFrom(feed, Some(loggedInUser))) {
            readFeedQueue.add(ReadFeedRequest(feed._id.stringify, loggedInUser._id.stringify, Some(FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES.toString), feed.last_read))
            new ModelAndView(new RedirectView(urlBuilder.getFeedUrl(feed)))
          } else {
            NotAllowed
          }
        }.getOrElse {
          NotFound
        }
      }
      Await.result(eventualResult, TenSeconds)
    }

    requiringAdminUser(accept)
  }

}
