package nz.co.searchwellington.filters.attributesetters

import nz.co.searchwellington.filters.RequestAttributeFilter
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import javax.servlet.http.HttpServletRequest

@Component
@Scope("request")
class PageParameterFilter extends RequestAttributeFilter {
  override def filter(request: HttpServletRequest) = if (request.getParameter(PageParameterFilter.PAGE_ATTRIBUTE) != null) {
    val pageString = request.getParameter(PageParameterFilter.PAGE_ATTRIBUTE)
    try {
      val page = pageString.toInt
      request.setAttribute(PageParameterFilter.PAGE_ATTRIBUTE, page)
    } catch {
      case e: NumberFormatException =>
    }
  }
}

object PageParameterFilter {
  val PAGE_ATTRIBUTE = "page"
}
