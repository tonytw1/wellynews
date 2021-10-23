package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.{ContentRetrievalService, TagDAO}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class TagsModelBuilder @Autowired()(tagDAO: TagDAO, frontendResourceMapper: FrontendResourceMapper,
                                               val contentRetrievalService: ContentRetrievalService) extends ModelBuilder with ReasonableWaits {

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches("^/tags$") || RequestPath.getPathFrom(request).matches("^/tags/json$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {
    for {
      tags <- tagDAO.getAllTags
    } yield {
      import scala.collection.JavaConverters._
      Some(new ModelAndView().
        addObject(MAIN_CONTENT, tags.asJava).
        addObject("heading", "All tags"))
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = {
    withLatestNewsitems(mv, loggedInUser)
  }

  def getViewName(mv: ModelAndView): String = "tags"

}
