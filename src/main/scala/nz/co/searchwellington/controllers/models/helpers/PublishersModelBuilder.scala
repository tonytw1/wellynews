package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Component class PublishersModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService,
                                                     frontendResourceMapper: FrontendResourceMapper,
                                                     loggedInUserFilter: LoggedInUserFilter) extends ModelBuilder with ReasonableWaits {

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/publishers$") || request.getPathInfo.matches("^/publishers/json$")
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {
    if (isValid(request)) {
      val mv = new ModelAndView
      import scala.collection.JavaConverters._
      val publishers = Await.result(contentRetrievalService.getAllPublishers(Option(loggedInUserFilter.getLoggedInUser)), TenSeconds).
        sortBy(_.title).
        map(p => frontendResourceMapper.createFrontendResourceFrom(p))
      mv.addObject(MAIN_CONTENT, publishers.asJava)
      mv.addObject("heading", "All publishers")
      Some(mv)

    } else {
      None
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
  }

  def getViewName(mv: ModelAndView): String = "publishers"

}
