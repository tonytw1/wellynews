package nz.co.searchwellington.controllers.models

import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.models.helpers.{CommonAttributesModelBuilder, CommonSizes}
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.collection.immutable

@Component class SearchModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService, urlBuilder: UrlBuilder, commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder with CommonSizes {

  private val KEYWORDS_PARAMETER = "keywords"

  def isValid(request: HttpServletRequest): Boolean = {
    request.getParameter(KEYWORDS_PARAMETER) != null
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {
    val mv = new ModelAndView()
    val keywords = request.getParameter(KEYWORDS_PARAMETER)
    val page = commonAttributesModelBuilder.getPage(request)
    mv.addObject("page", page)

    val startIndex = commonAttributesModelBuilder.getStartIndex(page)

    val tag: Option[Tag] =  if (request.getAttribute("tags") != null) (request.getAttribute("tags").asInstanceOf[Seq[Tag]].headOption) else None

    val contentWithCount: (Seq[FrontendResource], Int) = tag.fold { // The problem here is that you should be able to content and count in one go
      mv.addObject("related_tags", contentRetrievalService.getKeywordSearchFacets(keywords))

      val content = contentRetrievalService.getNewsitemsMatchingKeywords(keywords, startIndex, MAX_NEWSITEMS)
      val contentCount = contentRetrievalService.getNewsitemsMatchingKeywordsCount(keywords)
      (content, contentCount)


    }{ tag =>
      mv.addObject("tag", tag)

      val content = contentRetrievalService.getNewsitemsMatchingKeywords(keywords, tag, startIndex, MAX_NEWSITEMS)
      val contentCount = contentRetrievalService.getNewsitemsMatchingKeywordsCount(keywords, tag)
      (content, contentCount)

    }

    mv.addObject(MAIN_CONTENT, contentWithCount._1)

    val contentCount = contentWithCount._2
    mv.addObject("main_content_total", contentCount)
    commonAttributesModelBuilder.populatePagination(mv, startIndex, contentCount)

    /*
    if (startIndex > contentCount) {
      return null
    }
    */

    mv.addObject("query", keywords)
    mv.addObject("heading", "Search results - " + keywords)

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
