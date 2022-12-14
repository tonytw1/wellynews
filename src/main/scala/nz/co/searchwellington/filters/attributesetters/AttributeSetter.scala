package nz.co.searchwellington.filters.attributesetters

import javax.servlet.http.HttpServletRequest
import scala.concurrent.Future

trait AttributeSetter {

  def setAttributes(request: HttpServletRequest): Future[Boolean]

}
