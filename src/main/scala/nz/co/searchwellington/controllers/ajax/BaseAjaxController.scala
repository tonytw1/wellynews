package nz.co.searchwellington.controllers.ajax

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory

// TODO should be able to dog food these end points by consuming the json put of the main publishers and tags models
abstract class BaseAjaxController {

  private val TERM = "term"

  def viewFactory: ViewFactory

  def handleRequest(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val suggestions = Option(request.getParameter(TERM)).map { q =>
      getSuggestions(q)
    }.getOrElse(Seq.empty)

    val mv = new ModelAndView(viewFactory.getJsonView)
    import scala.collection.JavaConverters._
    mv.addObject("data", suggestions.asJava)
    mv
  }

  protected def getSuggestions(q: String): Seq[String]

}
