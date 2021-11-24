package nz.co.searchwellington.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory

@Controller class PingController @Autowired()(viewFactory: ViewFactory) {

  @GetMapping (value = Array ("/healthz") )
  def ping: ModelAndView = {
    new ModelAndView (viewFactory.getJsonView).addObject ("data", "ok")
  }

}