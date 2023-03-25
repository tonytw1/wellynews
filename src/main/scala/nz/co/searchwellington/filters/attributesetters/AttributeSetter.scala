package nz.co.searchwellington.filters.attributesetters

import jakarta.servlet.http.HttpServletRequest

import scala.concurrent.Future

trait AttributeSetter {

  def setAttributes(request: HttpServletRequest): Future[Map[String, Any]]

}
