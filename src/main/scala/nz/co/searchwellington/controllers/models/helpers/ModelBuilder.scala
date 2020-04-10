package nz.co.searchwellington.controllers.models

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.{ExecutionContext, Future}

trait ModelBuilder {

  val MAIN_CONTENT = "main_content"
  val LATEST_NEWSITEMS = "latest_newsitems"

  def contentRetrievalService: ContentRetrievalService

  def isValid(request: HttpServletRequest): Boolean

  def populateContentModel(request: HttpServletRequest, loggedInUser: User = null): Future[Option[ModelAndView]]

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView]

  def getViewName(mv: ModelAndView): String

  def withLatestNewsitems(mv: ModelAndView, loggedInUser: Option[User])(implicit ec: ExecutionContext): Future[ModelAndView] = {
    for {
      latestNewsitems <- contentRetrievalService.getLatestNewsitems(5, loggedInUser = loggedInUser)
    } yield {
      import scala.collection.JavaConverters._
      mv.addObject(LATEST_NEWSITEMS, latestNewsitems.asJava)
    }
  }

}
