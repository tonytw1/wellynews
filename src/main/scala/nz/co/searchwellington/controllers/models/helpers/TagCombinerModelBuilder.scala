package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.model.{Resource, Tag, User}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.tagging.RelatedTagsService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap

import java.util
import javax.servlet.http.HttpServletRequest
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Component class TagCombinerModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService, rssUrlBuilder: RssUrlBuilder,
                                                      val urlBuilder: UrlBuilder,
                                                      relatedTagsService: RelatedTagsService, commonAttributesModelBuilder: CommonAttributesModelBuilder)
  extends ModelBuilder with CommonSizes with ReasonableWaits {

  def isValid(request: HttpServletRequest): Boolean = {
    val tags = request.getAttribute("tags").asInstanceOf[Seq[Tag]]
    val isTagCombinerPage = tags != null && tags.size == 2
    isTagCombinerPage
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]] = {

    def populateTagCombinerModelAndView(tags: Seq[Tag]): Future[Option[ModelMap]] = {
      for {
        taggedNewsitemsAndCount <- contentRetrievalService.getTaggedNewsitems(tags.toSet, MAX_NEWSITEMS, loggedInUser)
      } yield {
        val totalNewsitemCount = taggedNewsitemsAndCount._2

        val firstTag = tags.head
        val secondTag = tags(1)

        if (totalNewsitemCount > 0) {
          val mv = new ModelMap().
            addAttribute("tag", firstTag).
            addAttribute("tags", tags.asJava).
            addAttribute("heading", firstTag.getDisplayName + " and " + secondTag.getDisplayName).
            addAttribute("description", "Items tagged with " + firstTag.getDisplayName + " and " + secondTag.getDisplayName + ".").
            addAttribute("link", urlBuilder.fullyQualified(urlBuilder.getTagCombinerUrl(firstTag, secondTag))).
            addAttribute(MAIN_CONTENT, taggedNewsitemsAndCount._1.asJava)

          commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForTagCombiner(firstTag, secondTag), rssUrlBuilder.getRssUrlForTagCombiner(firstTag, secondTag))
          Some(mv)

        } else {
          None
        }
      }
    }

    val tags = request.getAttribute("tags").asInstanceOf[Seq[Tag]]
    populateTagCombinerModelAndView(tags)
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[ModelMap] = {
    val tags = request.getAttribute("tags").asInstanceOf[Seq[Tag]]
    if (tags.nonEmpty) {
      val tag = tags.head
      val eventualTaggedWebsites = contentRetrievalService.getTaggedWebsites(tag, MAX_WEBSITES, loggedInUser = loggedInUser)
      val eventualLatestWebsites = contentRetrievalService.getLatestWebsites(5, loggedInUser = loggedInUser)
      val eventualRelatedTags = relatedTagsService.getRelatedTagsForTag(tag, 8, loggedInUser)
      for {
        taggedWebsites <- eventualTaggedWebsites
        latestWebsites <- eventualLatestWebsites
        relatedTags <- eventualRelatedTags
        latestNewsitems <- latestNewsitems(loggedInUser)
      } yield {
        new ModelMap().addAttribute("related_tags", relatedTags.asJava).
          addAttribute("websites", taggedWebsites.asJava).
          addAttribute("latest_news", latestWebsites._1.asJava).addAllAttributes(latestNewsitems)
      }
    } else {
      latestNewsitems(loggedInUser)
    }
  }

  def getViewName(mv: ModelMap, loggedInUser: Option[User]): String = {
    val taggedNewsitemsCount = mv.get("main_content_total").asInstanceOf[Long]
    val taggedWebsites = mv.get("websites").asInstanceOf[util.List[Resource]]
    val isOneContentType = taggedNewsitemsCount == 0 || taggedWebsites.size == 0
    if (isOneContentType) {
      "tagCombinedOneContentType"
    } else {
      "tag"
    }
  }

}
