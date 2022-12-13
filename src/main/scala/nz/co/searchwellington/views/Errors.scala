package nz.co.searchwellington.views

import org.springframework.web.servlet.ModelAndView

trait Errors {

  val NotFound = new ModelAndView("404")  // TODO
  val NotAllowed = new ModelAndView("403")  // TODO

}
