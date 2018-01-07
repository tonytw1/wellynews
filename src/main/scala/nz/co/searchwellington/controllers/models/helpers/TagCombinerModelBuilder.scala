package nz.co.searchwellington.controllers.models.helpers

import java.util
import java.util.{HashSet, List}
import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.controllers.{RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.model.{Resource, Tag}
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

@Component class TagCombinerModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService, rssUrlBuilder: RssUrlBuilder, urlBuilder: UrlBuilder, relatedTagsService: RelatedTagsService, commonAttributesModelBuilder: CommonAttributesModelBuilder)
  extends ModelBuilder with CommonSizes {

  def isValid(request: HttpServletRequest): Boolean = {
    val tags = request.getAttribute("tags").asInstanceOf[List[Tag]]
    val isTagCombinerPage = tags != null && tags.size == 2
    return isTagCombinerPage
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {

    def populateTagCombinerModelAndView(tags: List[Tag], page: Int): Option[ModelAndView] = {
      val startIndex = commonAttributesModelBuilder.getStartIndex(page)
      val totalNewsitemCount = contentRetrievalService.getTaggedNewsitemsCount(tags)

      if (startIndex > totalNewsitemCount) {
        None

      } else {
        val firstTag = tags.get(0)
        val secondTag = tags.get(1)

        if (totalNewsitemCount > 0) {
          val mv = new ModelAndView
          mv.addObject("tag", firstTag)
          mv.addObject("tags", tags)
          mv.addObject("heading", firstTag.getDisplayName + " and " + secondTag.getDisplayName)
          mv.addObject("description", "Items tagged with " + firstTag.getDisplayName + " and " + secondTag.getDisplayName + ".")
          mv.addObject("link", urlBuilder.getTagCombinerUrl(firstTag, secondTag))
          commonAttributesModelBuilder.populatePagination(mv, startIndex, totalNewsitemCount)

          val taggedNewsitems = contentRetrievalService.getTaggedNewsitems(tags, startIndex, MAX_NEWSITEMS)
          mv.addObject(MAIN_CONTENT, taggedNewsitems)
          commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForTagCombiner(tags.get(0), tags.get(1)), rssUrlBuilder.getRssUrlForTagCombiner(tags.get(0), tags.get(1)))
          Some(mv)

        } else {
          None
        }
      }
    }

    if (isValid(request)) {
      val tags = request.getAttribute("tags").asInstanceOf[List[Tag]]
      val page = commonAttributesModelBuilder.getPage(request)
      populateTagCombinerModelAndView(tags, page)
    } else {
      None
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    val tags: util.List[Tag] = request.getAttribute("tags").asInstanceOf[List[Tag]]
    if (!tags.isEmpty) {
      val tag = tags.get(0)
      mv.addObject("related_tags", relatedTagsService.getRelatedLinksForTag(tag, 8))
      mv.addObject("latest_news", contentRetrievalService.getLatestWebsites(5))
      val taggedWebsites = contentRetrievalService.getTaggedWebsites(new HashSet[Tag](tags), MAX_WEBSITES)
      mv.addObject("websites", taggedWebsites)
    }
  }

  def getViewName(mv: ModelAndView): String = {
    val taggedNewsitemsCount = mv.getModel.get("main_content_total").asInstanceOf[Long]
    val taggedWebsites = mv.getModel.get("websites").asInstanceOf[List[Resource]]
    val isOneContentType = taggedNewsitemsCount == 0 || taggedWebsites.size == 0
    if (isOneContentType) {
      return "tagCombinedOneContentType"
    }
    return "tag"
  }

}
