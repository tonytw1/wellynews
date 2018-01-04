package nz.co.searchwellington.controllers.models

import java.util.List
import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.controllers.models.helpers.CommonAttributesModelBuilder
import nz.co.searchwellington.model.Comment
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import nz.co.searchwellington.urls.UrlBuilder

@Component class SearchModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService, urlBuilder: UrlBuilder, commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder {

  private val KEYWORDS_PARAMETER = "keywords"

  def isValid(request: Nothing): Boolean = {
    request.getParameter(SearchModelBuilder.KEYWORDS_PARAMETER) != null
  }

  def populateContentModel(request: Nothing): Option[ModelAndView] = {
    val mv = new ModelAndView()
    val keywords = request.getParameter(SearchModelBuilder.KEYWORDS_PARAMETER)
    val page = commonAttributesModelBuilder.getPage(request)
    mv.addObject("page", page)

    if (request.getAttribute("tags") != null) {
      val tags = request.getAttribute("tags").asInstanceOf[Nothing]
      tag = tags.get(0)
      mv.addObject("tag", tag)
    }

    var contentCount: Int = 0
    if (request.getAttribute("tags") != null) {
      contentCount = contentRetrievalService.getNewsitemsMatchingKeywordsCount(keywords, tag)
    } else {
      contentCount = contentRetrievalService.getNewsitemsMatchingKeywordsCount(keywords)
    }

    val startIndex: Int = commonAttributesModelBuilder.getStartIndex(page)
    if (startIndex > contentCount) {
      return null
    }

    commonAttributesModelBuilder.populatePagination(mv, startIndex, contentCount)
    mv.addObject("query", keywords)
    mv.addObject("heading", "Search results - " + keywords)
    if (tag != null) {
      mv.addObject(MAIN_CONTENT,, contentRetrievalService.getNewsitemsMatchingKeywords(keywords, tag, startIndex, CommonAttributesModelBuilder.MAX_NEWSITEMS))
    } else {
      mv.addObject(MAIN_CONTENT,, contentRetrievalService.getNewsitemsMatchingKeywords(keywords, startIndex, CommonAttributesModelBuilder.MAX_NEWSITEMS))
      mv.addObject("related_tags", contentRetrievalService.getKeywordSearchFacets(keywords))
    }

    mv.addObject("main_content_total", contentCount)
    mv.addObject("main_heading", "Matching Newsitems")
    mv.addObject("main_description", "Found " + contentCount + " matching newsitems")
    mv.addObject("description", "Search results for '" + keywords + "'")
    mv.addObject("link", urlBuilder.getSearchUrlFor(keywords))
    return mv
  }

  @Override def populateExtraModelContent(request: Nothing, mv: Nothing) {
    mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5, 1))
  }

  @Override def getViewName(mv: Nothing): Nothing = {
    return "search"
  }

}