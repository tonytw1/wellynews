package nz.co.searchwellington.controllers.models

import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.models.helpers.CommonAttributesModelBuilder
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.collection.immutable

@Component class SearchModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService, urlBuilder: UrlBuilder, commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder {

  private val KEYWORDS_PARAMETER = "keywords"

  def isValid(request: HttpServletRequest): Boolean = {
    request.getParameter(KEYWORDS_PARAMETER) != null
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {
    val mv = new ModelAndView()
    val keywords = request.getParameter(KEYWORDS_PARAMETER)
    val page = commonAttributesModelBuilder.getPage(request)
    mv.addObject("page", page)

    //if (request.getAttribute("tags") != null) {
    //  val tags = request.getAttribute("tags").asInstanceOf[List[Tag]]
    //  val tag = tags(0)
    //  mv.addObject("tag", tag)
   // }

    var contentCount: Int = 0
    //if (request.getAttribute("tags") != null) {
     // val tags = request.getAttribute("tags").asInstanceOf[List[Tag]].toSeq
    //  val tag = tags(0)
    //  contentCount = contentRetrievalService.getNewsitemsMatchingKeywordsCount(keywords, tag)
   // } else {
      contentCount = contentRetrievalService.getNewsitemsMatchingKeywordsCount(keywords)
   // }

    val startIndex: Int = commonAttributesModelBuilder.getStartIndex(page)
    if (startIndex > contentCount) {
      return null
    }

    commonAttributesModelBuilder.populatePagination(mv, startIndex, contentCount)
    mv.addObject("query", keywords)
    mv.addObject("heading", "Search results - " + keywords)

    //if (request.getAttribute("tags") != null) {
    //  val tags = request.getAttribute("tags").asInstanceOf[List[Tag]].toSeq
    //  val tag = tags(0)
    //  if (tag != null) {
     //   mv.addObject(MAIN_CONTENT, contentRetrievalService.getNewsitemsMatchingKeywords(keywords, tag, startIndex, CommonAttributesModelBuilder.MAX_NEWSITEMS))
     // }

//    } else {
      mv.addObject(MAIN_CONTENT, contentRetrievalService.getNewsitemsMatchingKeywords(keywords, startIndex, CommonAttributesModelBuilder.MAX_NEWSITEMS))
      mv.addObject("related_tags", contentRetrievalService.getKeywordSearchFacets(keywords))
 //   }

    mv.addObject("main_content_total", contentCount)
    mv.addObject("main_heading", "Matching Newsitems")
    mv.addObject("main_description", "Found " + contentCount + " matching newsitems")
    mv.addObject("description", "Search results for '" + keywords + "'")
    mv.addObject("link", urlBuilder.getSearchUrlFor(keywords))
    Some(mv)
  }

  @Override def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5, 1))
  }

  @Override def getViewName(mv: ModelAndView): String = {
    return "search"
  }

}
