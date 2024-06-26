package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.model.PaginationLink
import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse, assertTrue}
import org.junit.jupiter.api.Test
import org.springframework.ui.ModelMap
import org.springframework.web.servlet.ModelAndView

import scala.jdk.CollectionConverters._

class PaginationTest extends Pagination {

  @Test
  def shouldProvidePaginationLinksForMainContent(): Unit = {
    val mv = new ModelMap()

    populatePagination(mv, 0, 100, 30, dummyLinkBuilder)

    val pageLinks = mv.get("page_links").asInstanceOf[java.util.List[PaginationLink]].asScala

    assertFalse(pageLinks.isEmpty)
    assertEquals(4, pageLinks.size)
    assertEquals(1, pageLinks.head.page)
    assertEquals("/something?page=1", pageLinks.head.url)
  }

  @Test
  def shouldOmitPageLinksIfOnlyOnePageOfMainContent(): Unit = {
    val mv = new ModelMap()

    populatePagination(mv, 0, 10, 30, dummyLinkBuilder)

    val pageLinks = mv.get("page_links").asInstanceOf[java.util.List[PaginationLink]].asScala

    assertTrue(pageLinks.isEmpty)
  }

  def dummyLinkBuilder(page: Int): String = {
    "/something?page=" + page
  }

}
