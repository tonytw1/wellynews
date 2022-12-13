package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.filters.PageParameterFilter
import nz.co.searchwellington.model.PaginationLink
import org.springframework.ui.ModelMap

import javax.servlet.http.HttpServletRequest
import scala.jdk.CollectionConverters._

trait Pagination {

  def getPage(request: HttpServletRequest): Int = {
    if (request.getAttribute(PageParameterFilter.PAGE_ATTRIBUTE) != null) {
      request.getAttribute(PageParameterFilter.PAGE_ATTRIBUTE).asInstanceOf[Integer]
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

  def populatePagination(mv: ModelMap, startIndex: Int, totalCount: Long, pageSize: Int, linkBuilder: Int => String): Unit = {
    mv.addAttribute("main_content_total", totalCount)
    mv.addAttribute("max_page_number", (totalCount / pageSize) + 1)
    val endIndex = if (startIndex + pageSize > totalCount) totalCount else startIndex + pageSize
    mv.addAttribute("start_index", startIndex + 1)
    mv.addAttribute("end_index", endIndex)

    mv.addAttribute("page_links", makePaginationLinks(totalCount, pageSize, linkBuilder).asJava)
  }

  private def makePaginationLinks(totalCount: Long, pageSize: Int, linkBuilder: Int => String): Seq[PaginationLink] = {
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
