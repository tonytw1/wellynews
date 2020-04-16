package nz.co.searchwellington.controllers.admin

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.feeds.FeedReader
import nz.co.searchwellington.filters.AdminRequestFilter
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy}
import nz.co.searchwellington.permissions.EditPermissionService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.ExecutionContext.Implicits.global

@Controller class AdminIndexController @Autowired()(requestFilter: AdminRequestFilter, feedReader: FeedReader, urlBuilder: UrlBuilder,
                                                    editPermissionService: EditPermissionService, loggedInUserFilter: LoggedInUserFilter) {

  private val log = Logger.getLogger(classOf[AdminIndexController])

  @RequestMapping(Array("/admin/feed/acceptall"))
  def acceptAllFrom(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    loggedInUserFilter.getLoggedInUser.map { loggedInUser =>
      if (loggedInUser.isAdmin) {
        val mv = new ModelAndView("adminindex")
        mv.addObject("heading", "Admin index")
        return mv

      } else {
        null // TODO 403
      }
    }.getOrElse {
      null // TODO 403
    }
  }

}
