package nz.co.searchwellington.controllers.ajax

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.controllers.LoggedInUserFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory

@Controller
class TitleAutocompleteAjaxController @Autowired()(viewFactory: ViewFactory, loggedInUserFilter: LoggedInUserFilter) {

  @RequestMapping(Array("/ajax/title-autofill"))
  def handleRequest(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    /* Given the url of a new page fetch it and try to extract the HTML title.
    This can be used to prefill the title field with submitted a new resource.
    Restrict to admin users to prevent abuse.
    */
    val title = "TODO"
    new ModelAndView(viewFactory.getJsonView).addObject("data", title)
  }

}
