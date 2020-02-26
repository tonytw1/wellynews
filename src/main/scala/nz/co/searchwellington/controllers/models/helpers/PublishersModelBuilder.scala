package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class PublishersModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService,
                                                     frontendResourceMapper: FrontendResourceMapper)
  extends ModelBuilder with ReasonableWaits {

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/publishers$") || request.getPathInfo.matches("^/publishers/json$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: User): Future[Option[ModelAndView]] = {
    if (isValid(request)) {
      for {
        publishers <- contentRetrievalService.getAllPublishers(Option(loggedInUser))
      } yield {
        val frontendPublishers = publishers.
          sortBy(_.title).
          map(frontendResourceMapper.createFrontendResourceFrom)
        import scala.collection.JavaConverters._
        val mv = new ModelAndView().
          addObject(MAIN_CONTENT, frontendPublishers.asJava).
          addObject("heading", "All publishers")
        Some(mv)
      }
    } else {
      Future.successful(None)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: User) {
  }

  def getViewName(mv: ModelAndView): String = "publishers"

}
