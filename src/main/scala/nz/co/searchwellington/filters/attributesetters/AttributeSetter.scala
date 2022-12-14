package nz.co.searchwellington.filters.attributesetters

import javax.servlet.http.HttpServletRequest

trait AttributeSetter {

  def setAttributes(request: HttpServletRequest): Boolean

}
