package nz.co.searchwellington.controllers.models.helpers

import java.awt.Component
import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.repositories.TagDAO
import org.apache.log4j.Logger

@Component class TagsModelBuilder @Autowired() (tagDAO: TagDAO) extends ModelBuilder {

  private val log: Logger = Logger.getLogger(classOf[TagsModelBuilder])

  private val MAIN_CONTENT: String = "main_content"

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/tags$") || request.getPathInfo.matches("^/tags/json$")
  }

  def populateContentModel(request: HttpServletRequest): ModelAndView = {
    if (isValid(request)) {
      return populateTagsPageModelAndView()
    }
    return null
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
  }

  def getViewName(mv: ModelAndView): String = {
    return "tags"
  }

  private def populateTagsPageModelAndView(): ModelAndView = {
    val mv: ModelAndView = new ModelAndView
    mv.addObject(MAIN_CONTENT, tagDAO.getAllTags)
    return mv
  }

}