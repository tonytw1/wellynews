package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.controllers.CommonModelObjectsService
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.ui.ModelMap

import javax.servlet.http.HttpServletRequest
import scala.concurrent.{ExecutionContext, Future}
import io.opentelemetry.api.trace.Span

trait ModelBuilder extends ContentFields with CommonModelObjectsService {

  def contentRetrievalService: ContentRetrievalService

  def isValid(request: HttpServletRequest): Boolean

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User] = None)(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]]

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[ModelMap]

  def getViewName(mv: ModelMap, loggedInUser: Option[User]): String

}
