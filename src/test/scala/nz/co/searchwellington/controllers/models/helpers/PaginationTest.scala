package nz.co.searchwellington.controllers.models.helpers

import org.junit.Test
import org.springframework.web.servlet.ModelAndView

class PaginationTest extends Pagination {

  @Test
  def shouldOmitPageLinksIfOnlyOnePageOfMainCotent {
    val mv = new ModelAndView()

    populatePagination(mv, 0, 10, 30, dummyLinkBuilder)

    val pageLinks = mv.getModel.get("page_links")
    fail("Should be empty")
  }

  def dummyLinkBuilder(page: Int): String = {
    "/?page=" + page
  }

}
