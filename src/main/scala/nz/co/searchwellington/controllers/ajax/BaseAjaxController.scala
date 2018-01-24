package nz.co.searchwellington.controllers.ajax

import java.io.IOException
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory

abstract class BaseAjaxController {

  private val TERM = "term"

  def viewFactory: ViewFactory

  @throws[IOException]
  def handleRequest(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView(viewFactory.getJsonView)
    if (request.getParameter(TERM) != null) {
      val suggestions = this.getSuggestions(request.getParameter(TERM))
      mv.addObject("data", suggestions)
    }
    mv
  }

  protected def getSuggestions(q: String): Seq[String]

}
