package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.User
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.{Await, ExecutionContext}

trait EditScreen extends CommonModelObjectsService with ReasonableWaits {

  def editScreen(viewname: String, heading: String, loggedInUser: Option[User])(implicit ec: ExecutionContext): ModelAndView = {
    val commonModel = Await.result(commonLocal, TenSeconds)

    new ModelAndView(viewname).
      addObject("heading", heading).
      addObject("loggedInUser", loggedInUser.orNull).
      addAllObjects(commonModel)
  }

}
