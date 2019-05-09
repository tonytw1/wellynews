package nz.co.searchwellington.views

import javax.servlet.http.HttpServletResponse
import org.springframework.web.servlet.ModelAndView

trait Errors {

  def NotFound(response: HttpServletResponse) = {
    response.setStatus(HttpServletResponse.SC_NOT_FOUND)  // TODO Not very functional; migrate to a full response return
    new ModelAndView("404")  // TODO
  }

}
