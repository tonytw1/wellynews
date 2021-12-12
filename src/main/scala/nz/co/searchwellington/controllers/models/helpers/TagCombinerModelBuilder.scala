package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.model.{Resource, Tag, User}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.tagging.RelatedTagsService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import java.util
import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

@Component class TagCombinerModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService, rssUrlBuilder: RssUrlBuilder,
                                                      val urlBuilder: UrlBuilder,
                                                      relatedTagsService: RelatedTagsService, commonAttributesModelBuilder: CommonAttributesModelBuilder)
  extends ModelBuilder with CommonSizes with Pagination with ReasonableWaits {

  def isValid(request: HttpServletRequest): Boolean = {
    val tags = request.getAttribute("tags").asInstanceOf[Seq[Tag]]
    val isTagCombinerPage = tags != null && tags.size == 2
    isTagCombinerPage
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {

    def populateTagCombinerModelAndView(tags: Seq[Tag], page: Int): Future[Option[ModelAndView]] = {
      val startIndex = getStartIndex(page, MAX_NEWSITEMS)
      for {
        taggedNewsitemsAndCount <- contentRetrievalService.getTaggedNewsitems(tags.toSet, startIndex, MAX_NEWSITEMS, loggedInUser)
      } yield {
        val totalNewsitemCount = taggedNewsitemsAndCount._2
        if (startIndex > totalNewsitemCount) {
          None

        } else {
          val firstTag = tags.head
          val secondTag = tags(1)

          if (totalNewsitemCount > 0) {
            val mv = new ModelAndView().
              addObject("tag", firstTag).
              addObject("tags", tags.asJava).
              addObject("heading", firstTag.getDisplayName + " and " + secondTag.getDisplayName).
              addObject("description", "Items tagged with " + firstTag.getDisplayName + " and " + secondTag.getDisplayName + ".").
              addObject("link", urlBuilder.fullyQualified(urlBuilder.getTagCombinerUrl(firstTag, secondTag))).
              addObject(MAIN_CONTENT, taggedNewsitemsAndCount._1.asJava)

            def paginationLinks(page: Int): String = urlBuilder.getTagCombinerUrl(firstTag, secondTag, page)
            populatePagination(mv, startIndex, totalNewsitemCount, MAX_NEWSITEMS, paginationLinks)
            commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForTagCombiner(firstTag, secondTag), rssUrlBuilder.getRssUrlForTagCombiner(firstTag, secondTag))
            Some(mv)

          } else {
            None
          }
        }
      }
    }

    val tags = request.getAttribute("tags").asInstanceOf[Seq[Tag]]
    val page = getPage(request)
    populateTagCombinerModelAndView(tags, page)
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = {
    val tags = request.getAttribute("tags").asInstanceOf[Seq[Tag]]
    val eventualWithExtras = if (tags.nonEmpty) {
      val tag = tags.head
      val eventualTaggedWebsites = contentRetrievalService.getTaggedWebsites(tag, MAX_WEBSITES, loggedInUser = loggedInUser)
      val eventualLatestWebsites = contentRetrievalService.getLatestWebsites(5, loggedInUser = loggedInUser)
      val eventualRelatedTags = relatedTagsService.getRelatedTagsForTag(tag, 8, loggedInUser)
      for {
        taggedWebsites <- eventualTaggedWebsites
        latestWebsites <- eventualLatestWebsites
        relatedTags <- eventualRelatedTags
      } yield {
        mv.addObject("related_tags", relatedTags.asJava)
        mv.addObject("websites", taggedWebsites.asJava)
        mv.addObject("latest_news", latestWebsites._1.asJava)
        mv
      }
    } else {
      Future.successful(mv)
    }

    eventualWithExtras.flatMap(withLatestNewsitems(_, loggedInUser))
  }

  def getViewName(mv: ModelAndView, loggedInUser: Option[User]): String = {
    val taggedNewsitemsCount = mv.getModel.get("main_content_total").asInstanceOf[Long]
    val taggedWebsites = mv.getModel.get("websites").asInstanceOf[util.List[Resource]]
    val isOneContentType = taggedNewsitemsCount == 0 || taggedWebsites.size == 0
    if (isOneContentType) {
      "tagCombinedOneContentType"
    } else {
      "tag"
    }
  }

}
