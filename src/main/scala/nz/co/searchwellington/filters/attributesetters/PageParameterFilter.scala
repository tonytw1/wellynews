package nz.co.searchwellington.filters.attributesetters

import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import javax.servlet.http.HttpServletRequest
import scala.concurrent.Future

@Component
@Scope("request")
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
