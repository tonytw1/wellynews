package nz.co.searchwellington.controllers.models

import javax.servlet.http.HttpServletRequest
import org.springframework.web.servlet.ModelAndView

trait ModelBuilder {

  def isValid(request: Nothing): Boolean
  def populateContentModel(request: HttpServletRequest): Option[ModelAndView]
  def populateExtraModelContent(request: Nothing, mv: ModelAndView)
  def getViewName(mv: ModelAndView): String

}