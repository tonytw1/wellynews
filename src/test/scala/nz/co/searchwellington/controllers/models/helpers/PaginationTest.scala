package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.model.PaginationLink
import org.junit.Assert.assertTrue
import org.junit.Test
import org.springframework.web.servlet.ModelAndView

class PaginationTest extends Pagination {

  @Test
  def shouldOmitPageLinksIfOnlyOnePageOfMainCotent {
    val mv = new ModelAndView()

    populatePagination(mv, 0, 10, 30, dummyLinkBuilder)

    import scala.collection.JavaConverters._
    val pageLinks = mv.getModel.get("page_links").asInstanceOf[java.util.List[PaginationLink]].asScala

    println(pageLinks)
    assertTrue(pageLinks.isEmpty)
  }

  def dummyLinkBuilder(page: Int): String = {
    "/something?page=" + page
  }

}
