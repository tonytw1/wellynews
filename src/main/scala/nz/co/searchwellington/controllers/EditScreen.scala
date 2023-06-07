package nz.co.searchwellington.controllers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.User
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.{Await, ExecutionContext}

trait EditScreen extends CommonModelObjects with ReasonableWaits {

  def editScreen(viewname: String, heading: String, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): ModelAndView = {
    val eventualCommonModel = commonLocal
    val eventualLatestNewsitems = latestNewsitems(loggedInUser)

    Await.result(for {
      commonModel <- eventualCommonModel
      latestNewsitems <- eventualLatestNewsitems
    } yield {
      new ModelAndView(viewname).
        addObject("heading", heading).
        addObject("loggedInUser", loggedInUser.orNull).
        addAllObjects(commonModel).
        addAllObjects(latestNewsitems)
    }, TenSeconds)
  }

}
