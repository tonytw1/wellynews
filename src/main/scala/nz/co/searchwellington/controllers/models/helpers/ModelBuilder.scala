package nz.co.searchwellington.controllers.models

import javax.servlet.http.HttpServletRequest
import org.springframework.web.servlet.ModelAndView

trait ModelBuilder {

  val MAIN_CONTENT = "main_content"

  def isValid(request: HttpServletRequest): Boolean
  def populateContentModel(request: HttpServletRequest): Option[ModelAndView]
  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView)
  def getViewName(mv: ModelAndView): String

}
