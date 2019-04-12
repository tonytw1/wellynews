package nz.co.searchwellington.controllers.ajax

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory

abstract class BaseAjaxController {

  private val TERM = "term"

  def viewFactory: ViewFactory

  def handleRequest(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val suggestions = if (request.getParameter(TERM) != null) {
      getSuggestions(request.getParameter(TERM))
    } else {
      Seq.empty
    }

    val mv = new ModelAndView(viewFactory.getJsonView)
    import scala.collection.JavaConverters._
    mv.addObject("data", suggestions.asJava)
    mv
  }

  protected def getSuggestions(q: String): Seq[String]

}
