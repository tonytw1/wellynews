package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest

import org.springframework.web.servlet.ModelAndView

trait Pagination extends CommonSizes {

  def getPage(request: HttpServletRequest): Int = {
    if (request.getAttribute("page") != null) {
      request.getAttribute("page").asInstanceOf[Integer]
    } else {
      0
    }
  }

  def getStartIndex(page: Int): Int = {
    if (page > 1) {
      (page - 1) * MAX_NEWSITEMS
    } else {
      0
    }
  }

  def populatePagination(mv: ModelAndView, startIndex: Int, totalNewsitemCount: Long) {
    mv.addObject("main_content_total", totalNewsitemCount)
    mv.addObject("max_page_number", ((totalNewsitemCount / 30) + 1))
    val endIndex = if (startIndex + MAX_NEWSITEMS > totalNewsitemCount) totalNewsitemCount else startIndex + MAX_NEWSITEMS
    mv.addObject("start_index", startIndex + 1)
    mv.addObject("end_index", endIndex)
  }

}
