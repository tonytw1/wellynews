package nz.co.searchwellington.controllers.models.helpers

import java.util.List

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.controllers.{RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, RssfeedNewsitemService}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Resource, Tag, User}
import nz.co.searchwellington.repositories.{ContentRetrievalService, TagDAO}
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class TagModelBuilder @Autowired()(rssUrlBuilder: RssUrlBuilder, val urlBuilder: UrlBuilder,
                                              relatedTagsService: RelatedTagsService, rssfeedNewsitemService: RssfeedNewsitemService,
                                              val contentRetrievalService: ContentRetrievalService, feedItemLocalCopyDecorator: FeedItemLocalCopyDecorator,
                                              commonAttributesModelBuilder: CommonAttributesModelBuilder, tagDAO: TagDAO,
                                              frontendResourceMapper: FrontendResourceMapper) extends ModelBuilder
  with CommonSizes with Pagination with ReasonableWaits {

  private val PAGE = "page"
  private val TAG = "tag"
  private val TAGS = "tags"
  private val TAG_WATCHLIST = "tag_watchlist"
  private val TAG_FEEDS = "tag_feeds"
  private val WEBSITES = "websites"

  def isValid(request: HttpServletRequest): Boolean = {
    val tags = request.getAttribute(TAGS).asInstanceOf[Seq[Tag]]
    tags != null && tags.size == 1
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {

    def populateTagPageModelAndView(tag: Tag, page: Int): Future[Option[ModelAndView]] = {
      val startIndex = getStartIndex(page, MAX_NEWSITEMS)

      val eventualTaggedNewsitems = contentRetrievalService.getTaggedNewsitems(tag, startIndex, MAX_NEWSITEMS, loggedInUser)
      val eventualChildTags = tagDAO.loadTagsByParent(tag._id)
      val eventualMaybeParent = tag.parent.map { pid =>
        tagDAO.loadTagByObjectId(pid)
      }.getOrElse {
        Future.successful(None)
      }

      for {
        taggedNewsitemsAndTotalCount <- eventualTaggedNewsitems
        children <- eventualChildTags
        maybeParent <- eventualMaybeParent

      } yield {
        val totalNewsitems = taggedNewsitemsAndTotalCount._2
        if (startIndex > totalNewsitems) {
          None

        } else {
          val mv = new ModelAndView().
            addObject(PAGE, page).
            addObject(TAG, tag).
            addObject("heading", tag.display_name).
            addObject("description", rssUrlBuilder.getRssDescriptionForTag(tag)).
            addObject("link", urlBuilder.getTagUrl(tag)).
            addObject("parent", maybeParent.orNull)

          val taggedNewsitems = taggedNewsitemsAndTotalCount._1
          import scala.collection.JavaConverters._
          mv.addObject(MAIN_CONTENT, taggedNewsitems.asJava)

          def paginationLinks(page: Int): String = {
            urlBuilder.getTagPageUrl(tag, page)
          }
          populatePagination(mv, startIndex, totalNewsitems, MAX_NEWSITEMS, paginationLinks)
          if (taggedNewsitems.nonEmpty) {
            commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForTag(tag), rssUrlBuilder.getRssUrlForTag(tag))
          }

          if (children.nonEmpty) {
            import scala.collection.JavaConverters._
            mv.addObject("children", children.asJava)
          }

          Some(mv)
        }
      }
    }

    populateTagPageModelAndView(tagFromRequest(request), getPage(request))
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = {
    val tag = tagFromRequest(request)

    val eventualGeotaggedNewsitems = contentRetrievalService.getGeotaggedNewsitemsForTag(tag, MAX_NUMBER_OF_GEOTAGGED_TO_SHOW, loggedInUser = loggedInUser)
    val eventualTaggedWebsites = contentRetrievalService.getTaggedWebsites(tag, MAX_WEBSITES, loggedInUser)
    val eventualRelatedTagLinks = relatedTagsService.getRelatedTagsForTag(tag, 8, loggedInUser)
    val eventualRelatedPublishersForTag = relatedTagsService.getRelatedPublishersForTag(tag, 8, loggedInUser)
    val eventualTagWatchlist = contentRetrievalService.getTagWatchlist(tag, loggedInUser)
    val eventualTagFeeds = contentRetrievalService.getTaggedFeeds(tag, loggedInUser)
    val eventualLatestNewsitems = contentRetrievalService.getLatestNewsitems(5, loggedInUser = loggedInUser)

    for {
      geotaggedNewsitems <- eventualGeotaggedNewsitems
      taggedWebsites <- eventualTaggedWebsites
      relatedTagLinks <- eventualRelatedTagLinks
      relatedPublishersForTag <- eventualRelatedPublishersForTag
      tagWatchList <- eventualTagWatchlist
      tagFeeds <- eventualTagFeeds
      latestNewsitems <- eventualLatestNewsitems

    } yield {
      def populateGeocoded(mv: ModelAndView, tag: Tag) {
        if (geotaggedNewsitems.nonEmpty) {
          import scala.collection.JavaConverters._
          mv.addObject("geocoded", geotaggedNewsitems.asJava)
        }
      }

      import scala.collection.JavaConverters._
      mv.addObject(WEBSITES, taggedWebsites.asJava)

      if (relatedTagLinks.nonEmpty) {
        mv.addObject("related_tags", relatedTagLinks.asJava)
      }
      if (relatedPublishersForTag.nonEmpty) {
        mv.addObject("related_publishers", relatedPublishersForTag.asJava)
      }
      populateGeocoded(mv, tag)
      mv.addObject(TAG_WATCHLIST, tagWatchList.asJava)
      mv.addObject(TAG_FEEDS, tagFeeds.asJava)
      mv.addObject("latest_newsitems", latestNewsitems.asJava)
    }
  }

  def getViewName(mv: ModelAndView): String = {
    val mainContent = mv.getModel.get(MAIN_CONTENT).asInstanceOf[List[Resource]]
    val taggedWebsites = mv.getModel.get(WEBSITES).asInstanceOf[List[Resource]]
    val tagWatchlist = mv.getModel.get(TAG_WATCHLIST).asInstanceOf[List[Resource]]
    val tagFeeds = mv.getModel.get(TAG_FEEDS).asInstanceOf[List[Resource]]

    val hasSecondaryContent = !taggedWebsites.isEmpty || !tagWatchlist.isEmpty || !tagFeeds.isEmpty
    val isOneContentType = mainContent.isEmpty || !hasSecondaryContent
    val page = mv.getModel.get(PAGE).asInstanceOf[Integer]

    if (page != null && page > 1) {
      mv.addObject(PAGE, page)
      "tagNewsArchive"
    } else if (isOneContentType) {
      "tagOneContentType"
    } else {
      "tag"
    }
  }

  private def tagFromRequest(request: HttpServletRequest): Tag = {
    val tags = request.getAttribute(TAGS).asInstanceOf[Seq[Tag]]
    tags.head
  }

}
