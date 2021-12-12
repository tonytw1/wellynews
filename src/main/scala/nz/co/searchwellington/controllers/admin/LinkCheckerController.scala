package nz.co.searchwellington.controllers.admin

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RequiringLoggedInUser, UrlStack}
import nz.co.searchwellington.filters.AdminRequestFilter
import nz.co.searchwellington.model.{Resource, User}
import nz.co.searchwellington.queues.LinkCheckerQueue
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.ExecutionContext.Implicits.global

@Controller class LinkCheckerController @Autowired()(requestFilter: AdminRequestFilter, queue: LinkCheckerQueue, urlStack: UrlStack,
                                                     val loggedInUserFilter: LoggedInUserFilter) extends RequiringLoggedInUser {

  private val log = LogFactory.getLog(classOf[LinkCheckerController])

  @RequestMapping(Array("/admin/linkchecker/add"))
  def addToQueue(request: HttpServletRequest): ModelAndView = {
    def add(loggedInUser: User): ModelAndView = {
      requestFilter.loadAttributesOntoRequest(request)
      if (request.getAttribute("resource") != null) {
        val resource = request.getAttribute("resource").asInstanceOf[Resource]
        log.info("Adding resource to queue: " + resource.id + "(" + resource._id.stringify + ")")
        queue.add(resource._id.stringify)
      } else {
        log.warn("No resource found on request; not adding to queue")
      }
      new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)))
    }

    requiringAdminUser(add)
  }

}
