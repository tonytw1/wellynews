package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.controllers.CommonModelObjectsService
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.ui.ModelMap
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import scala.concurrent.Future

trait ModelBuilder extends ContentFields with CommonModelObjectsService {

  def contentRetrievalService: ContentRetrievalService

  def isValid(request: HttpServletRequest): Boolean

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User] = None): Future[Option[ModelAndView]]

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User]): Future[ModelMap]

  def getViewName(mv: ModelAndView, loggedInUser: Option[User]): String

}
