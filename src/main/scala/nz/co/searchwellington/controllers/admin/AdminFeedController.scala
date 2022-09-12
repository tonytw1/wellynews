package nz.co.searchwellington.controllers.admin

import io.opentelemetry.api.trace.Span

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RequiringLoggedInUser}
import nz.co.searchwellington.feeds.FeedReader
import nz.co.searchwellington.filters.AdminRequestFilter
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, User}
import nz.co.searchwellington.permissions.EditPermissionService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.ExecutionContext.Implicits.global

@Controller class AdminFeedController @Autowired()(requestFilter: AdminRequestFilter, feedReader: FeedReader, urlBuilder: UrlBuilder,
                                                   editPermissionService: EditPermissionService, val loggedInUserFilter: LoggedInUserFilter)
  extends RequiringLoggedInUser {

  private val log = LogFactory.getLog(classOf[AdminFeedController])

  @RequestMapping(Array("/admin/feed/accept-all"))
  def acceptAllFrom(request: HttpServletRequest): ModelAndView = {
    implicit val currentSpan: Span = Span.current()
    def accept(loggedInUser: User): ModelAndView = {
      requestFilter.loadAttributesOntoRequest(request)
      if (request.getAttribute("feedAttribute") == null) throw new RuntimeException("Not found") // TODO
      val feed = request.getAttribute("feedAttribute").asInstanceOf[Feed]
      if (!editPermissionService.canAcceptAllFrom(feed, Some(loggedInUser))) {
        log.warn("Not allowed to read this feed") // TODO return http auth error
        throw new RuntimeException("Not allowed")
      }
      feedReader.processFeed(feed, loggedInUser, Some(FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES))
      new ModelAndView(new RedirectView(urlBuilder.getFeedUrl(feed)))
    }

    requiringAdminUser(accept)
  }

}
