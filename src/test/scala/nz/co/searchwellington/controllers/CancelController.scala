package nz.co.searchwellington.controllers

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

@Controller
class CancelController @Autowired()(urlStack: UrlStack) {

  @RequestMapping(Array("/cancel"))
  def cancel(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)))
  }
}
