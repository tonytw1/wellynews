package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.TagDAO
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.Await

@Component class TagsModelBuilder @Autowired()(tagDAO: TagDAO, frontendResourceMapper: FrontendResourceMapper) extends ModelBuilder with ReasonableWaits {

  private val log = Logger.getLogger(classOf[TagsModelBuilder])

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/tags$") || request.getPathInfo.matches("^/tags/json$")
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {
    if (isValid(request)) {
      val mv = new ModelAndView
      val allFrontendTags = tagDAO.getAllTags
      import scala.collection.JavaConverters._
      mv.addObject(MAIN_CONTENT, Await.result(allFrontendTags, TenSeconds).asJava)
      mv.addObject("heading", "All tags")
      Some(mv)

    } else {
      None
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
  }

  def getViewName(mv: ModelAndView): String = "tags"

}
