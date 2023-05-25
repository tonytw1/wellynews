package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.frontend.Action
import nz.co.searchwellington.repositories.{ContentRetrievalService, TagDAO}
import nz.co.searchwellington.urls.AdminUrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Component class TagsModelBuilder @Autowired()(tagDAO: TagDAO, val contentRetrievalService: ContentRetrievalService,
                                               adminUrlBuilder: AdminUrlBuilder) extends ModelBuilder
  with ReasonableWaits {

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches("^/tags$") || RequestPath.getPathFrom(request).matches("^/tags/json$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]] = {
    for {
      tags <- tagDAO.getAllTags
    } yield {
      val mv = new ModelMap().
        addAttribute(MAIN_CONTENT, tags.asJava).
        addAttribute("heading", "All tags")
      if (loggedInUser.exists(_.isAdmin)) {
        mv.addAttribute("actions", Seq(Action("Add new tag", adminUrlBuilder.getAddTagUrl)).asJava)
      }
      Some(mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[ModelMap] = {
    latestNewsitems(loggedInUser)
  }

  def getViewName(mv: ModelMap, loggedInUser: Option[User]): String = "tags"

}
