package nz.co.searchwellington.controllers.models.helpers

import java.util.List

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.model.{Tag, User}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class TagGeotaggedModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService,
                                                       urlBuilder: UrlBuilder, rssUrlBuilder: RssUrlBuilder,
                                                       commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder
  with CommonSizes with ReasonableWaits {

  private val log = Logger.getLogger(classOf[TagGeotaggedModelBuilder])

  def isValid(request: HttpServletRequest): Boolean = {
    val tags = request.getAttribute("tags").asInstanceOf[List[Tag]]
    val isSingleTagPage = tags != null && tags.size == 1
    val hasCommentPath = request.getPathInfo.matches("^(.*?)/geotagged(/(rss|json))?$")
    isSingleTagPage && hasCommentPath
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: User): Future[Option[ModelAndView]] = {
    def populateTagCommentPageModelAndView(tag: Tag): Future[Some[ModelAndView]] = {
      for {
        newsitems <- contentRetrievalService.getGeotaggedNewsitemsForTag(tag, MAX_NUMBER_OF_GEOTAGGED_TO_SHOW, loggedInUser = Option(loggedInUser))
      } yield {
        val mv = new ModelAndView().
          addObject("tag", tag).
          addObject("heading", tag.getDisplayName + " geotagged").
          addObject("description", "Geotagged " + tag.getDisplayName + " newsitems").
          addObject("link", urlBuilder.getTagCommentUrl(tag)).
          addObject(MAIN_CONTENT, newsitems)
        if (newsitems.size > 0) {
          commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForTagGeotagged(tag), rssUrlBuilder.getRssUrlForTagGeotagged(tag))
        }
        Some(mv)
      }
    }

    log.debug("Building tag geotagged page model")
    val tags = request.getAttribute("tags").asInstanceOf[List[Tag]]
    val tag = tags.get(0)
    populateTagCommentPageModelAndView(tag)
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: User): Future[ModelAndView] = {
    Future.successful(mv)
  }

  def getViewName(mv: ModelAndView): String = "tagGeotagged"

}
