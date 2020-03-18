package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
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
    for {
      publishers <- contentRetrievalService.getAllPublishers(Option(loggedInUser))
      frontendPublishers <- Future.sequence {
        publishers.
          sortBy(_.title).
          map(frontendResourceMapper.createFrontendResourceFrom)
      }
    } yield {
      import scala.collection.JavaConverters._
      val mv = new ModelAndView().
        addObject(MAIN_CONTENT, frontendPublishers.asJava).
        addObject("heading", "All publishers")
      Some(mv)
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

  def getViewName(mv: ModelAndView): String = "publishers"

}
