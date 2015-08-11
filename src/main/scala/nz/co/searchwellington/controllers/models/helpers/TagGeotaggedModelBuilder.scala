package nz.co.searchwellington.controllers.models.helpers

import java.util.List
import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import nz.co.searchwellington.controllers.models.ModelBuilder

@Component class TagGeotaggedModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService, urlBuilder: UrlBuilder, rssUrlBuilder: RssUrlBuilder, commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder {

  private val log: Logger = Logger.getLogger(classOf[TagGeotaggedModelBuilder])

  @SuppressWarnings(Array("unchecked")) def isValid(request: HttpServletRequest): Boolean = {
    val tags: List[Tag] = request.getAttribute("tags").asInstanceOf[List[Tag]]
    val isSingleTagPage: Boolean = tags != null && tags.size == 1
    val hasCommentPath: Boolean = request.getPathInfo.matches("^(.*?)/geotagged(/(rss|json))?$")
    return isSingleTagPage && hasCommentPath
  }

  @SuppressWarnings(Array("unchecked")) def populateContentModel(request: HttpServletRequest): ModelAndView = {
    if (isValid(request)) {
      log.debug("Building tag geotagged page model")
      val tags: List[Tag] = request.getAttribute("tags").asInstanceOf[List[Tag]]
      val tag: Tag = tags.get(0)
      return populateTagCommentPageModelAndView(tag)
    }
    return null
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
  }

  def getViewName(mv: ModelAndView): String = {
    return "tagGeotagged"
  }

  private def populateTagCommentPageModelAndView(tag: Tag): ModelAndView = {
    val mv: ModelAndView = new ModelAndView
    mv.addObject("tag", tag)
    mv.addObject("heading", tag.getDisplayName + " geotagged")
    mv.addObject("description", "Geotagged " + tag.getDisplayName + " newsitems")
    mv.addObject("link", urlBuilder.getTagCommentUrl(tag))
    val allGeotaggedForTag: List[FrontendResource] = contentRetrievalService.getTaggedGeotaggedNewsitems(tag, CommonAttributesModelBuilder.MAX_NUMBER_OF_GEOTAGGED_TO_SHOW)
    mv.addObject("main_content", allGeotaggedForTag)
    if (allGeotaggedForTag.size > 0) {
      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForTagGeotagged(tag), rssUrlBuilder.getRssUrlForTagGeotagged(tag))
    }
    return mv
  }

}