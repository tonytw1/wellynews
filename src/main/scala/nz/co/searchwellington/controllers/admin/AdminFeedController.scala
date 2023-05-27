package nz.co.searchwellington.controllers.admin

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RequiringLoggedInUser}
import nz.co.searchwellington.feeds.FeedReader
import nz.co.searchwellington.filters.attributesetters.FeedAttributeSetter
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, User}
import nz.co.searchwellington.permissions.EditPermissionService
import nz.co.searchwellington.urls.UrlBuilder
import nz.co.searchwellington.views.Errors
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.ExecutionContext.Implicits.global

@Order(6)
@Controller class AdminFeedController @Autowired()(feedReader: FeedReader,
                                                   urlBuilder: UrlBuilder,
                                                   editPermissionService: EditPermissionService,
                                                   val loggedInUserFilter: LoggedInUserFilter)
  extends RequiringLoggedInUser with Errors {

  @RequestMapping(Array("/feed/*/accept-all"))
  def acceptAllFrom(request: HttpServletRequest): ModelAndView = {
    implicit val currentSpan: Span = Span.current()

    def accept(loggedInUser: User): ModelAndView = {
      if (request.getAttribute(FeedAttributeSetter.FEED_ATTRIBUTE) != null) {
        val feed = request.getAttribute(FeedAttributeSetter.FEED_ATTRIBUTE).asInstanceOf[Feed]
        if (editPermissionService.canAcceptAllFrom(feed, Some(loggedInUser))) {
          feedReader.processFeed(feed, loggedInUser, Some(FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES))
          new ModelAndView(new RedirectView(urlBuilder.getFeedUrl(feed)))

        } else {
          NotAllowed
        }
      } else {
        NotFound
      }
    }

    requiringAdminUser(accept)
  }

}
