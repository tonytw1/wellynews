package nz.co.searchwellington.controllers.models.helpers

import java.util.List

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.model.{Resource, Tag}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.Await

@Component class TagCombinerModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService, rssUrlBuilder: RssUrlBuilder, urlBuilder: UrlBuilder,
                                                      relatedTagsService: RelatedTagsService, commonAttributesModelBuilder: CommonAttributesModelBuilder,
                                                      loggedInUserFilter: LoggedInUserFilter)
  extends ModelBuilder with CommonSizes with Pagination with ReasonableWaits {

  def isValid(request: HttpServletRequest): Boolean = {
    val tags = request.getAttribute("tags").asInstanceOf[Seq[Tag]]
    val isTagCombinerPage = tags != null && tags.size == 2
    isTagCombinerPage
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {

    def populateTagCombinerModelAndView(tags: Seq[Tag], page: Int): Option[ModelAndView] = {
      val startIndex = getStartIndex(page)
      val taggedNewsitemsAndCount = Await.result(contentRetrievalService.getTaggedNewsitems(tags.toSet, startIndex, MAX_NEWSITEMS, Option(loggedInUserFilter.getLoggedInUser)), TenSeconds)
      val totalNewsitemCount = taggedNewsitemsAndCount._2

      if (startIndex > totalNewsitemCount) {
        None

      } else {
        val firstTag = tags.head
        val secondTag = tags(1)

        if (totalNewsitemCount > 0) {
          val mv = new ModelAndView
          mv.addObject("tag", firstTag)
          mv.addObject("tags", tags)
          mv.addObject("heading", firstTag.getDisplayName + " and " + secondTag.getDisplayName)
          mv.addObject("description", "Items tagged with " + firstTag.getDisplayName + " and " + secondTag.getDisplayName + ".")
          mv.addObject("link", urlBuilder.getTagCombinerUrl(firstTag, secondTag))
          populatePagination(mv, startIndex, totalNewsitemCount)

          val taggedNewsitems = taggedNewsitemsAndCount._1
          mv.addObject(MAIN_CONTENT, taggedNewsitems)
          commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForTagCombiner(firstTag, secondTag), rssUrlBuilder.getRssUrlForTagCombiner(firstTag, secondTag))
          Some(mv)

        } else {
          None
        }
      }
    }

    if (isValid(request)) {
      val tags = request.getAttribute("tags").asInstanceOf[Seq[Tag]]
      val page = getPage(request)
      populateTagCombinerModelAndView(tags, page)

    } else {
      None
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    val tags = request.getAttribute("tags").asInstanceOf[Seq[Tag]]
    if (tags.nonEmpty) {
      val tag = tags.head
      mv.addObject("related_tags", relatedTagsService.getRelatedTagsForTag(tag, 8, Option(loggedInUserFilter.getLoggedInUser)))
      import scala.collection.JavaConverters._
      mv.addObject("latest_news", Await.result(contentRetrievalService.getLatestWebsites(5, loggedInUser = Option(loggedInUserFilter.getLoggedInUser)), TenSeconds).asJava)
      val taggedWebsites = contentRetrievalService.getTaggedWebsites(tag, MAX_WEBSITES, loggedInUser = Option(loggedInUserFilter.getLoggedInUser))
      mv.addObject("websites", taggedWebsites)
    }
  }

  def getViewName(mv: ModelAndView): String = {
    val taggedNewsitemsCount = mv.getModel.get("main_content_total").asInstanceOf[Long]
    val taggedWebsites = mv.getModel.get("websites").asInstanceOf[List[Resource]]
    val isOneContentType = taggedNewsitemsCount == 0 || taggedWebsites.size == 0
    if (isOneContentType) {
      "tagCombinedOneContentType"
    } else {
      "tag"
    }
  }

}
