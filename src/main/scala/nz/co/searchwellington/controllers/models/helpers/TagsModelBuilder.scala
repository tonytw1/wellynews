package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.TagDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class TagsModelBuilder @Autowired()(tagDAO: TagDAO, frontendResourceMapper: FrontendResourceMapper) extends ModelBuilder with ReasonableWaits {

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/tags$") || request.getPathInfo.matches("^/tags/json$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: User): Future[Option[ModelAndView]] = {
    if (isValid(request)) {
      for {
        tags <- tagDAO.getAllTags
      } yield {
        import scala.collection.JavaConverters._
        Some(new ModelAndView().
          addObject(MAIN_CONTENT, tags.asJava).
          addObject("heading", "All tags"))
      }
    } else {
      Future.successful(None)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: User) {
  }

  def getViewName(mv: ModelAndView): String = "tags"

}
