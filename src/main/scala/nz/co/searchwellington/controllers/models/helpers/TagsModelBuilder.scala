package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.model.frontend.FrontendTag
import nz.co.searchwellington.repositories.TagDAO
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

@Component class TagsModelBuilder @Autowired()(tagDAO: TagDAO) extends ModelBuilder {

  private val log = Logger.getLogger(classOf[TagsModelBuilder])

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/tags$") || request.getPathInfo.matches("^/tags/json$")
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {

    def populateTagsPageModelAndView(): ModelAndView = {
      val mv = new ModelAndView
      val allTags = tagDAO.getAllTags.toList
      mv.addObject(MAIN_CONTENT, allTags.map(t => new FrontendTag(t.getName, t.getDisplayName)).asJava)
      mv.addObject("heading", "All tags")
      mv
    }

    if (isValid(request)) {
      Some(populateTagsPageModelAndView())
    } else {
      None
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
  }

  def getViewName(mv: ModelAndView): String = {
    "tags"
  }

}
