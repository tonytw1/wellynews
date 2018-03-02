package nz.co.searchwellington.controllers.admin

import java.io.IOException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import nz.co.searchwellington.controllers.UrlStack
import nz.co.searchwellington.filters.AdminRequestFilter
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.queues.LinkCheckerQueue
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

@Controller class LinkCheckerController @Autowired()(requestFilter: AdminRequestFilter, queue: LinkCheckerQueue, urlStack: UrlStack) {

  private val log = Logger.getLogger(classOf[LinkCheckerController])

  @RequestMapping(Array("/admin/linkchecker/add"))
  @throws[IOException]
  def addToQueue(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    requestFilter.loadAttributesOntoRequest(request)
    if (request.getAttribute("resource") != null) {
      val resource = request.getAttribute("resource").asInstanceOf[Resource]
      log.info("Adding resource to queue: " + resource.id + "(" + resource.id + ")")
      queue.add(resource.id)
    } else {
      log.warn("No resource found on request; not adding to queue")
    }
    new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)))
  }

}
