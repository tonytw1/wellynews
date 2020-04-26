package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.model.PaginationLink
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import org.springframework.web.servlet.ModelAndView

class PaginationTest extends Pagination {

  @Test
  def shouldProvidePaginationLinksForMainContent() {
    val mv = new ModelAndView()

    populatePagination(mv, 0, 100, 30, dummyLinkBuilder)

    import scala.collection.JavaConverters._
    val pageLinks = mv.getModel.get("page_links").asInstanceOf[java.util.List[PaginationLink]].asScala

    assertFalse(pageLinks.isEmpty)
    assertEquals(4, pageLinks.size)
    assertEquals(1, pageLinks.head.page)
    assertEquals("/something?page=1", pageLinks.head.url)
  }

  @Test
  def shouldOmitPageLinksIfOnlyOnePageOfMainContent() {
    val mv = new ModelAndView()

    populatePagination(mv, 0, 10, 30, dummyLinkBuilder)

    import scala.collection.JavaConverters._
    val pageLinks = mv.getModel.get("page_links").asInstanceOf[java.util.List[PaginationLink]].asScala

    assertTrue(pageLinks.isEmpty)
  }

  def dummyLinkBuilder(page: Int): String = {
    "/something?page=" + page
  }

}
