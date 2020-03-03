package nz.co.searchwellington.controllers.models

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.model.User
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.Future

trait ModelBuilder {

  val MAIN_CONTENT = "main_content"

  def isValid(request: HttpServletRequest): Boolean
  def populateContentModel(request: HttpServletRequest, loggedInUser: User = null): Future[Option[ModelAndView]]
  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: User): Future[ModelAndView]
  def getViewName(mv: ModelAndView): String

}
