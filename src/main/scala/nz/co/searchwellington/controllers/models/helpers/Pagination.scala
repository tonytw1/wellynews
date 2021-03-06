package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.model.PaginationLink
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.web.servlet.ModelAndView

trait Pagination {

  def getPage(request: HttpServletRequest): Int = {
    if (request.getAttribute("page") != null) {
      request.getAttribute("page").asInstanceOf[Integer]
    } else {
      1
    }
  }

  def getStartIndex(page: Int, pageSize: Int): Int = {
    if (page > 1) {
      (page - 1) * pageSize
    } else {
      0
    }
  }

  def populatePagination(mv: ModelAndView, startIndex: Int, totalCount: Long, pageSize: Int, linkBuilder: Int => String) {
    mv.addObject("main_content_total", totalCount)
    mv.addObject("max_page_number", (totalCount / pageSize) + 1)
    val endIndex = if (startIndex + pageSize > totalCount) totalCount else startIndex + pageSize
    mv.addObject("start_index", startIndex + 1)
    mv.addObject("end_index", endIndex)

    import scala.collection.JavaConverters._
    mv.addObject("page_links", makePaginationLinks(startIndex, totalCount, pageSize, linkBuilder).asJava)
  }

  private def makePaginationLinks(startIndex: Int, totalCount: Long, pageSize: Int, linkBuilder: Int => String): Seq[PaginationLink] = {
    val maxPageNumber = (totalCount / pageSize) + 1
    if (maxPageNumber > 1) {
      val pages = 1 to maxPageNumber.toInt
      pages.map { page =>
        PaginationLink(page, linkBuilder(page))
      }
    } else {
      Seq.empty
    }
  }

}
