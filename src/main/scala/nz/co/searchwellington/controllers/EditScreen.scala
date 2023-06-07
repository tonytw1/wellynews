package nz.co.searchwellington.controllers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.TagDAO
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.{Await, ExecutionContext}
import scala.jdk.CollectionConverters._

trait EditScreen extends CommonModelObjects with ReasonableWaits {

  def tagDAO: TagDAO

  def editScreen(viewname: String, heading: String, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): ModelAndView = {
    val eventualCommonModel = commonLocal
    val eventualLatestNewsitems = latestNewsitems(loggedInUser)

    Await.result(for {
      commonModel <- eventualCommonModel
      latestNewsitems <- eventualLatestNewsitems
      tags <- tagDAO.getAllTags()
    } yield {
      new ModelAndView(viewname).
        addObject("heading", heading).
        addObject("loggedInUser", loggedInUser.orNull).
        addObject("tags", tags.asJava).
        addAllObjects(commonModel).
        addAllObjects(latestNewsitems)
    }, TenSeconds)
  }

}
