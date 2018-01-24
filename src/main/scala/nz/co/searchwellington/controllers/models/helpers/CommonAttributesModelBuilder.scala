package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView


@Component class CommonAttributesModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService) extends CommonSizes {

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

  def setRss(mv: ModelAndView, title: String, url: String) {
    mv.addObject("rss_title", title)
    mv.addObject("rss_url", url)
  }

  def populatePagination(mv: ModelAndView, startIndex: Int, totalNewsitemCount: Long) {
    mv.addObject("main_content_total", totalNewsitemCount)
    mv.addObject("max_page_number", ((totalNewsitemCount / 30) + 1))
    val endIndex = if (startIndex + MAX_NEWSITEMS > totalNewsitemCount) totalNewsitemCount else startIndex + MAX_NEWSITEMS
    mv.addObject("start_index", startIndex + 1)
    mv.addObject("end_index", endIndex)
  }

  final def populateSecondaryFeeds(mv: ModelAndView) {
    mv.addObject("righthand_heading", "Local Feeds")
    mv.addObject("righthand_description", "Recently updated feeds from local organisations.")
    val allFeeds = contentRetrievalService.getAllFeedsOrderByLatestItemDate
    if (allFeeds != null && allFeeds.size > 0) {
      mv.addObject("righthand_content", allFeeds)
    }
  }

}
