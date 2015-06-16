package nz.co.searchwellington.controllers.models.helpers

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

@Component class TagCombinerModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService, rssUrlBuilder: RssUrlBuilder, urlBuilder: UrlBuilder, relatedTagsService: RelatedTagsService, commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder {

  @SuppressWarnings(Array("unchecked")) def isValid(request: HttpServletRequest): Boolean = {
    val tags: List[Tag] = request.getAttribute("tags").asInstanceOf[List[Tag]]
    val isTagCombinerPage: Boolean = tags != null && tags.size == 2
    return isTagCombinerPage
  }

  @SuppressWarnings(Array("unchecked")) def populateContentModel(request: HttpServletRequest): ModelAndView = {
    if (isValid(request)) {
      val tags: List[Tag] = request.getAttribute("tags").asInstanceOf[List[Tag]]
      val page: Int = commonAttributesModelBuilder.getPage(request)
      return populateTagCombinerModelAndView(tags, page)
    }
    return null
  }

  @SuppressWarnings(Array("unchecked")) def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    val tags: List[Tag] = request.getAttribute("tags").asInstanceOf[List[Tag]]
    val tag: Tag = tags.get(0)
    mv.addObject("related_tags", relatedTagsService.getRelatedLinksForTag(tag, 8))
    mv.addObject("latest_news", contentRetrievalService.getLatestWebsites(5))
    val taggedWebsites: List[FrontendResource] = contentRetrievalService.getTaggedWebsites(new HashSet[Tag](tags), CommonAttributesModelBuilder.MAX_WEBSITES)
    mv.addObject("websites", taggedWebsites)
  }

  @SuppressWarnings(Array("unchecked")) def getViewName(mv: ModelAndView): String = {
    val taggedNewsitemsCount: Long = mv.getModel.get("main_content_total").asInstanceOf[Long]
    val taggedWebsites: List[Resource] = mv.getModel.get("websites").asInstanceOf[List[Resource]]
    val isOneContentType: Boolean = taggedNewsitemsCount == 0 || taggedWebsites.size == 0
    if (isOneContentType) {
      return "tagCombinedOneContentType"
    }
    return "tag"
  }

  private def populateTagCombinerModelAndView(tags: List[Tag], page: Int): ModelAndView = {
    val startIndex: Int = commonAttributesModelBuilder.getStartIndex(page)
    val totalNewsitemCount: Long = contentRetrievalService.getTaggedNewsitemsCount(tags)
    if (startIndex > totalNewsitemCount) {
      return null
    }
    val firstTag: Tag = tags.get(0)
    val secondTag: Tag = tags.get(1)
    val mv: ModelAndView = new ModelAndView
    mv.addObject("tag", firstTag)
    mv.addObject("tags", tags)
    mv.addObject("heading", firstTag.getDisplayName + " and " + secondTag.getDisplayName)
    mv.addObject("description", "Items tagged with " + firstTag.getDisplayName + " and " + secondTag.getDisplayName + ".")
    mv.addObject("link", urlBuilder.getTagCombinerUrl(firstTag, secondTag))
    if (totalNewsitemCount > 0) {
      commonAttributesModelBuilder.populatePagination(mv, startIndex, totalNewsitemCount)
      val taggedNewsitems: List[FrontendResource] = contentRetrievalService.getTaggedNewsitems(tags, startIndex, CommonAttributesModelBuilder.MAX_NEWSITEMS)
      mv.addObject("main_content", taggedNewsitems)
      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForTagCombiner(tags.get(0), tags.get(1)), rssUrlBuilder.getRssUrlForTagCombiner(tags.get(0), tags.get(1)))
      return mv
    }
    return null
  }

}