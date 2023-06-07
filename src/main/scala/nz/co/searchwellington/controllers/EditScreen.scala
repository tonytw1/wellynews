package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.TagDAO
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.{Await, ExecutionContext}
import scala.jdk.CollectionConverters._

trait EditScreen extends ReasonableWaits {

  def tagDAO: TagDAO
  def editScreen(viewname: String, heading: String, loggedInUser: Option[User])(implicit ec: ExecutionContext): ModelAndView = {
    new ModelAndView(viewname).
      addObject("heading", heading).
      addObject("loggedInUser", loggedInUser.orNull).
      addObject("tags", Await.result(tagDAO.getAllTags, TenSeconds).asJava)
  }

}
