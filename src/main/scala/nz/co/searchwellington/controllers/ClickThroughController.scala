package nz.co.searchwellington.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

@deprecated
@Controller class ClickThroughController @Autowired()(urlBuilder: UrlBuilder) {

  private val log = Logger.getLogger(classOf[ClickThroughController])

  @RequestMapping(Array("/clickthrough"))
  def handleRequest(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val resource = request.getAttribute("resource").asInstanceOf[Resource]

    new ModelAndView(new RedirectView(resource.page))
  }

}
