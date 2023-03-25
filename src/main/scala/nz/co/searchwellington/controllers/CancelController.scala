package nz.co.searchwellington.controllers

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

@Order(4)
@Controller
class CancelController @Autowired()(urlStack: UrlStack) {

  @RequestMapping(Array("/cancel"))
  def cancel(request: HttpServletRequest): ModelAndView = {
    new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)))
  }
}
