package nz.co.searchwellington.controllers.models

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.controllers.models.helpers.{CommonSizes, Pagination}
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.Await

@Component class SearchModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService, urlBuilder: UrlBuilder, loggedInUserFilter: LoggedInUserFilter)
  extends ModelBuilder with CommonSizes with Pagination with ReasonableWaits {

  private val KEYWORDS_PARAMETER = "keywords"

  def isValid(request: HttpServletRequest): Boolean = {
    request.getParameter(KEYWORDS_PARAMETER) != null
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {
    val mv = new ModelAndView()
    val keywords = request.getParameter(KEYWORDS_PARAMETER)
    val page = getPage(request)
    mv.addObject("page", page)

    val startIndex = getStartIndex(page)

    val maybeTag = Option(request.getAttribute("tags")).flatMap { t =>
      t.asInstanceOf[Seq[Tag]].headOption
    }

    val contentWithCount = maybeTag.fold {
      mv.addObject("related_tags", contentRetrievalService.getKeywordSearchFacets(keywords))
      contentRetrievalService.getNewsitemsMatchingKeywords(keywords, startIndex, MAX_NEWSITEMS, Option(loggedInUserFilter.getLoggedInUser))

    } { tag =>
      mv.addObject("tag", tag)
      contentRetrievalService.getTagNewsitemsMatchingKeywords(keywords, tag, startIndex, MAX_NEWSITEMS, Option(loggedInUserFilter.getLoggedInUser))
    }

    import scala.collection.JavaConverters._
    mv.addObject(MAIN_CONTENT, contentWithCount._1.asJava)

    val contentCount = contentWithCount._2
    mv.addObject("main_content_total", contentCount)
    populatePagination(mv, startIndex, contentCount)

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
    import scala.collection.JavaConverters._
    mv.addObject("latest_newsitems", Await.result(contentRetrievalService.getLatestNewsitems(5, loggedInUser = Option(loggedInUserFilter.getLoggedInUser)), TenSeconds).asJava)
  }

  @Override def getViewName(mv: ModelAndView): String = "search"

}
