package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

@Component class PublishersModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                     frontendResourceMapper: FrontendResourceMapper)
  extends ModelBuilder with ReasonableWaits {

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches("^/publishers$") || RequestPath.getPathFrom(request).matches("^/publishers/json$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {
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
      val mv = new ModelAndView().
        addObject(MAIN_CONTENT, frontendPublishers.asJava).
        addObject("heading", "All publishers")
      Some(mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = {
    withLatestNewsitems(mv, loggedInUser)
  }

  def getViewName(mv: ModelAndView, loggedInUser: Option[User]): String = "publishers"

}
