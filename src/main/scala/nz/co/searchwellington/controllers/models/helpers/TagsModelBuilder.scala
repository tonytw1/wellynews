package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.{ContentRetrievalService, TagDAO}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class TagsModelBuilder @Autowired()(tagDAO: TagDAO, frontendResourceMapper: FrontendResourceMapper,
                                               contentRetrievalService: ContentRetrievalService) extends ModelBuilder with ReasonableWaits {

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/tags$") || request.getPathInfo.matches("^/tags/json$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: User): Future[Option[ModelAndView]] = {
    for {
      tags <- tagDAO.getAllTags
    } yield {
      import scala.collection.JavaConverters._
      Some(new ModelAndView().
        addObject(MAIN_CONTENT, tags.asJava).
        addObject("heading", "All tags"))
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: User): Future[ModelAndView] = {
    for {
      latestNewsitems <- contentRetrievalService.getLatestNewsitems(5, loggedInUser = Option(loggedInUser))
    } yield {
      import scala.collection.JavaConverters._
      mv.addObject("latest_newsitems", latestNewsitems.asJava)
    }
  }

  def getViewName(mv: ModelAndView): String = "tags"

}
