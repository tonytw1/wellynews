package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.{Tag, User}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import java.util.List
import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class TagGeotaggedModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                       urlBuilder: UrlBuilder, rssUrlBuilder: RssUrlBuilder,
                                                       commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder
  with CommonSizes with ReasonableWaits {

  private val log = Logger.getLogger(classOf[TagGeotaggedModelBuilder])

  def isValid(request: HttpServletRequest): Boolean = {
    val tags = request.getAttribute("tags").asInstanceOf[List[Tag]]
    val isSingleTagPage = tags != null && tags.size == 1
    val hasCommentPath = RequestPath.getPathFrom(request).matches("^(.*?)/geotagged(/(rss|json))?$")
    isSingleTagPage && hasCommentPath
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {
    def populateTagCommentPageModelAndView(tag: Tag): Future[Some[ModelAndView]] = {
      for {
        newsitems <- contentRetrievalService.getGeotaggedNewsitemsForTag(tag, MAX_NUMBER_OF_GEOTAGGED_TO_SHOW, loggedInUser = loggedInUser)
      } yield {
        val mv = new ModelAndView().
          addObject("tag", tag).
          addObject("heading", tag.getDisplayName + " geotagged").
          addObject("description", "Geotagged " + tag.getDisplayName + " newsitems").
          addObject("link", urlBuilder.fullyQualified(urlBuilder.getTagGeocodedUrl(tag))).
          addObject(MAIN_CONTENT, newsitems)

        if (newsitems.nonEmpty) {
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

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = {
    Future.successful(mv)
  }

  def getViewName(mv: ModelAndView, loggedInUser: Option[User]): String = "tagGeotagged"

}
