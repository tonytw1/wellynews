package nz.co.searchwellington.filters.attributesetters

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

import scala.concurrent.Future

@Component
class PageParameterFilter extends AttributeSetter {

  override def setAttributes(request: HttpServletRequest): Future[Map[String, Any]] = {
    if (request.getParameter(PageParameterFilter.PAGE_ATTRIBUTE) != null) {
      val pageString = request.getParameter(PageParameterFilter.PAGE_ATTRIBUTE)
      try {
        Future.successful(Map(
          PageParameterFilter.PAGE_ATTRIBUTE -> pageString.toInt
        ))
      } catch {
        case e: NumberFormatException =>
          Future.successful(Map.empty)
      }
    } else {
      Future.successful(Map.empty)
    }
  }
}

object PageParameterFilter {
  val PAGE_ATTRIBUTE = "page"
}
