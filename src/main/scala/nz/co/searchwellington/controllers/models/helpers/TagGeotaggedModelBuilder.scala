package nz.co.searchwellington.controllers.models.helpers

import java.util.List

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.Await

@Component class TagGeotaggedModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService,
                                                       urlBuilder: UrlBuilder, rssUrlBuilder: RssUrlBuilder,
                                                       commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder with CommonSizes with ReasonableWaits {

  private val log = Logger.getLogger(classOf[TagGeotaggedModelBuilder])

  def isValid(request: HttpServletRequest): Boolean = {
    val tags = request.getAttribute("tags").asInstanceOf[List[Tag]]
    val isSingleTagPage = tags != null && tags.size == 1
    val hasCommentPath = request.getPathInfo.matches("^(.*?)/geotagged(/(rss|json))?$")
    isSingleTagPage && hasCommentPath
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {

    def populateTagCommentPageModelAndView(tag: Tag): ModelAndView = {
      val mv = new ModelAndView
      mv.addObject("tag", tag)
      mv.addObject("heading", tag.getDisplayName + " geotagged")
      mv.addObject("description", "Geotagged " + tag.getDisplayName + " newsitems")
      mv.addObject("link", urlBuilder.getTagCommentUrl(tag))
      val allGeotaggedForTag = Await.result(contentRetrievalService.getGeotaggedNewsitemsForTag(tag, MAX_NUMBER_OF_GEOTAGGED_TO_SHOW), TenSeconds)
      mv.addObject(MAIN_CONTENT, allGeotaggedForTag)
      if (allGeotaggedForTag.size > 0) {
        commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForTagGeotagged(tag), rssUrlBuilder.getRssUrlForTagGeotagged(tag))
      }
      mv
    }

    if (isValid(request)) {
      log.debug("Building tag geotagged page model")
      val tags = request.getAttribute("tags").asInstanceOf[List[Tag]]
      val tag = tags.get(0)
      Some(populateTagCommentPageModelAndView(tag))
    } else {
      None
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
  }

  def getViewName(mv: ModelAndView): String = {
    "tagGeotagged"
  }

}
