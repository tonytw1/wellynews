package nz.co.searchwellington.controllers.models.helpers

import java.util.List

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.controllers.{RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.model.{Resource, Tag, User}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class TagCombinerModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService, rssUrlBuilder: RssUrlBuilder, urlBuilder: UrlBuilder,
                                                      relatedTagsService: RelatedTagsService, commonAttributesModelBuilder: CommonAttributesModelBuilder)
  extends ModelBuilder with CommonSizes with Pagination with ReasonableWaits {

  private val logger = Logger.getLogger(classOf[TagCombinerModelBuilder])

  def isValid(request: HttpServletRequest): Boolean = {
    val tags = request.getAttribute("tags").asInstanceOf[Seq[Tag]]
    logger.info("Is valid tags: " + tags)
    val isTagCombinerPage = tags != null && tags.size == 2
    logger.info("Is valid: " + isTagCombinerPage)
    isTagCombinerPage
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: User): Future[Option[ModelAndView]] = {

    def populateTagCombinerModelAndView(tags: Seq[Tag], page: Int): Future[Option[ModelAndView]] = {
      val startIndex = getStartIndex(page, MAX_NEWSITEMS)
      for {
        taggedNewsitemsAndCount <- contentRetrievalService.getTaggedNewsitems(tags.toSet, startIndex, MAX_NEWSITEMS, Option(loggedInUser))
      } yield {
        val totalNewsitemCount = taggedNewsitemsAndCount._2
        if (startIndex > totalNewsitemCount) {
          None

        } else {
          val firstTag = tags.head
          val secondTag = tags(1)

          if (totalNewsitemCount > 0) {
            import scala.collection.JavaConverters._
            val mv = new ModelAndView().
              addObject("tag", firstTag).
              addObject("tags", tags.asJava).
              addObject("heading", firstTag.getDisplayName + " and " + secondTag.getDisplayName).
              addObject("description", "Items tagged with " + firstTag.getDisplayName + " and " + secondTag.getDisplayName + ".").
              addObject("link", urlBuilder.getTagCombinerUrl(firstTag, secondTag)).
              addObject(MAIN_CONTENT, taggedNewsitemsAndCount._1.asJava)

            populatePagination(mv, startIndex, totalNewsitemCount, MAX_NEWSITEMS)
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

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: User): Future[ModelAndView] = {
    val tags = request.getAttribute("tags").asInstanceOf[Seq[Tag]]
    if (tags.nonEmpty) {
      val tag = tags.head
      val eventualTaggedWebsites = contentRetrievalService.getTaggedWebsites(tag, MAX_WEBSITES, loggedInUser = Option(loggedInUser))
      val eventualLatestWebsites = contentRetrievalService.getLatestWebsites(5, loggedInUser = Option(loggedInUser))
      val eventualRelatedTags = relatedTagsService.getRelatedTagsForTag(tag, 8, Option(loggedInUser))
      for {
        taggedWebsites <- eventualTaggedWebsites
        latestWebsites <- eventualLatestWebsites
        relatedTags <- eventualRelatedTags
      } yield {
        import scala.collection.JavaConverters._
        mv.addObject("related_tags", relatedTags.asJava)
        mv.addObject("websites", taggedWebsites.asJava)
        mv.addObject("latest_news", latestWebsites.asJava)
        mv
      }
    } else {
      Future.successful(mv)
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
