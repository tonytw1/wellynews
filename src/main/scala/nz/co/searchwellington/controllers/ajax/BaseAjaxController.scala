package nz.co.searchwellington.controllers.ajax

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.model.User
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

// TODO should be able to dog food these end points by consuming the json put of the main publishers and tags models
abstract class BaseAjaxController extends ReasonableWaits {

  private val TERM = "term"

  def loggedInUserFilter: LoggedInUserFilter

  def viewFactory: ViewFactory

  def handleRequest(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val eventualSuggestions = Option(request.getParameter(TERM)).map { q =>
      getSuggestions(q, loggedInUserFilter.getLoggedInUser)
    }.getOrElse {
      Future.successful(Seq.empty)
    }

    Await.result(for {
      suggestions <- eventualSuggestions
    } yield {
      val mv = new ModelAndView(viewFactory.getJsonView)
      mv.addObject("data", suggestions.asJava)
      mv
    }, TenSeconds)
  }

  protected def getSuggestions(q: String, loggedInUser: Option[User]): Future[Seq[String]]

}
