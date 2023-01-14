package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.filters.attributesetters.TagPageAttributeSetter
import nz.co.searchwellington.model.{Tag, User}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap

import javax.servlet.http.HttpServletRequest
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Component class TagGeotaggedModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                       urlBuilder: UrlBuilder, rssUrlBuilder: RssUrlBuilder,
                                                       commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder
  with CommonSizes with ReasonableWaits {

  private val log = LogFactory.getLog(classOf[TagGeotaggedModelBuilder])

  def isValid(request: HttpServletRequest): Boolean = {
    val tags = request.getAttribute(TagPageAttributeSetter.TAGS).asInstanceOf[Seq[Tag]]
    val isSingleTagPage = tags != null && tags.size == 1
    val hasGeotaggedSuffix = RequestPath.getPathFrom(request).matches("^(.*?)/geotagged(/(rss|json))?$")
    isSingleTagPage && hasGeotaggedSuffix
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]] = {
    def populateTagCommentPageModelAndView(tag: Tag): Future[Some[ModelMap]] = {
      for {
        newsitems <- contentRetrievalService.getGeotaggedNewsitemsForTag(tag, MAX_NUMBER_OF_GEOTAGGED_TO_SHOW, loggedInUser = loggedInUser)
      } yield {
        val mv = new ModelMap().
          addAttribute("tag", tag).
          addAttribute("heading", tag.getDisplayName + " geotagged").
          addAttribute("description", "Geotagged " + tag.getDisplayName + " newsitems").
          addAttribute("link", urlBuilder.fullyQualified(urlBuilder.getTagGeocodedUrl(tag))).
          addAttribute(MAIN_CONTENT, newsitems.asJava)

        if (newsitems.nonEmpty) {
          commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForTagGeotagged(tag), rssUrlBuilder.getRssUrlForTagGeotagged(tag))
        }
        Some(mv)
      }
    }

    log.debug("Building tag geotagged page model")
    val tags = request.getAttribute(TagPageAttributeSetter.TAGS).asInstanceOf[Seq[Tag]]
    val tag = tags.head
    populateTagCommentPageModelAndView(tag)
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[ModelMap] = {
    latestNewsitems(loggedInUser)
  }

  def getViewName(mv: ModelMap, loggedInUser: Option[User]): String = "tagGeotagged"

}
