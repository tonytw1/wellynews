package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Component class PublishersModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                     frontendResourceMapper: FrontendResourceMapper)
  extends ModelBuilder with ReasonableWaits {

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches("^/publishers$") || RequestPath.getPathFrom(request).matches("^/publishers/json$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]] = {
    val q = Option(request.getParameter("q"))

    val eventualPublishers = q.map { q =>
      contentRetrievalService.getPublisherNamesByStartingLetters(q, loggedInUser)
    }.getOrElse {
      contentRetrievalService.getAllPublishers(loggedInUser)
    }

    for {
      publishers <- eventualPublishers
      frontendPublishers <- Future.sequence {
        publishers.
          sortBy(_.title).
          map(r => frontendResourceMapper.createFrontendResourceFrom(r, loggedInUser))
      }
    } yield {
      Some(new ModelMap().
        addAttribute(MAIN_CONTENT, frontendPublishers.asJava).
        addAttribute("heading", "All publishers"))
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[ModelMap] = {
    latestNewsitems(loggedInUser)
  }

  def getViewName(mv: ModelMap, loggedInUser: Option[User]): String = "publishers"

}
