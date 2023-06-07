package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.controllers.CommonModelObjects
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.ui.ModelMap

import scala.concurrent.{ExecutionContext, Future}

trait ModelBuilder extends ContentFields with CommonModelObjects {

  def contentRetrievalService: ContentRetrievalService

  def isValid(request: HttpServletRequest): Boolean

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User] = None)(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]]

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[ModelMap]

  def getViewName(mv: ModelMap, loggedInUser: Option[User]): String

}
