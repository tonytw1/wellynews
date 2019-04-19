package nz.co.searchwellington.controllers.models.helpers

import java.util.List
import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.controllers.{RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, RssfeedNewsitemService}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Resource, Tag}
import nz.co.searchwellington.repositories.{ContentRetrievalService, TagDAO}
import nz.co.searchwellington.urls.UrlBuilder
import nz.co.searchwellington.views.GeocodeToPlaceMapper
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

@Component class TagModelBuilder @Autowired()(rssUrlBuilder: RssUrlBuilder, urlBuilder: UrlBuilder,
                                              relatedTagsService: RelatedTagsService, rssfeedNewsitemService: RssfeedNewsitemService,
                                              contentRetrievalService: ContentRetrievalService, feedItemLocalCopyDecorator: FeedItemLocalCopyDecorator,
                                              geocodeToPlaceMapper: GeocodeToPlaceMapper,
                                              commonAttributesModelBuilder: CommonAttributesModelBuilder, tagDAO: TagDAO,
                                              frontendResourceMapper: FrontendResourceMapper) extends ModelBuilder
  with CommonSizes with Pagination {

  private val log = Logger.getLogger(classOf[TagModelBuilder])

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

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {

    def populateTagPageModelAndView(tag: Tag, page: Int): Option[ModelAndView] = {
      val mv = new ModelAndView
      mv.addObject(PAGE, page)
      val startIndex = getStartIndex(page)
      val totalNewsitemCount = contentRetrievalService.getTaggedNewitemsCount(tag)

      if (startIndex > totalNewsitemCount) {
        None

      } else {
        mv.addObject(TAG, frontendResourceMapper.mapTagToFrontendTag(tag))

        tag.geocode.map { g =>
          mv.addObject("location", geocodeToPlaceMapper.mapGeocodeToPlace(g))
        }

        mv.addObject("heading", tag.display_name)
        mv.addObject("description", rssUrlBuilder.getRssDescriptionForTag(tag))
        mv.addObject("link", urlBuilder.getTagUrl(tag))

        tag.parent.map { pid =>
          tagDAO.loadTagByObjectId(pid).map { p =>
            mv.addObject("parent", frontendResourceMapper.mapTagToFrontendTag(p))
          }
        }

        val children = tagDAO.loadTagsByParent(tag._id).map(t => frontendResourceMapper.mapTagToFrontendTag(t))
        if (children.nonEmpty) {
          import scala.collection.JavaConverters._
          mv.addObject("children", children.asJava)
        }

        val taggedNewsitems = contentRetrievalService.getTaggedNewsitems(tag, startIndex, MAX_NEWSITEMS)
        log.info("Got tagged newsitems: " + taggedNewsitems.size)
        import scala.collection.JavaConverters._
        mv.addObject(MAIN_CONTENT, taggedNewsitems.asJava)

        populatePagination(mv, startIndex, totalNewsitemCount)
        if (taggedNewsitems.nonEmpty) {
          commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForTag(tag), rssUrlBuilder.getRssUrlForTag(tag))
        }
        Some(mv)
      }
    }

    if (isValid(request)) {
      val page = getPage(request)
      populateTagPageModelAndView(tagFromRequest(request), page)

    } else {
      None
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {

    def populateGeocoded(mv: ModelAndView, tag: Tag) {
      val geocoded = contentRetrievalService.getTaggedGeotaggedNewsitems(tag, MAX_NUMBER_OF_GEOTAGGED_TO_SHOW)
      log.debug("Found " + geocoded.size + " valid geocoded resources for tag: " + tag.getName)
      if (geocoded.nonEmpty) {
        mv.addObject("geocoded", geocoded)
      }
    }

    def populateCommentedTaggedNewsitems(mv: ModelAndView, tag: Tag) {
      val recentCommentedNewsitems = contentRetrievalService.getRecentCommentedNewsitemsForTag(tag, MAX_NUMBER_OF_COMMENTED_TO_SHOW_IN_RHS + 1)

      val commentedToShow = recentCommentedNewsitems.take(MAX_NUMBER_OF_COMMENTED_TO_SHOW_IN_RHS)

      val commentsCount = contentRetrievalService.getCommentedNewsitemsForTagCount(tag)
      val moreCommentCount: Int = commentsCount - commentedToShow.size
      if (moreCommentCount > 0) {
        mv.addObject("commented_newsitems_morecount", moreCommentCount)
        mv.addObject("commented_newsitems_moreurl", urlBuilder.getTagCommentUrl(tag))
      }
      mv.addObject("commented_newsitems", commentedToShow)
    }

    val tag = tagFromRequest(request)
    val taggedWebsites = contentRetrievalService.getTaggedWebsites(tag, MAX_WEBSITES)
    log.info("Tag websites: " + taggedWebsites.size)
    import scala.collection.JavaConverters._
    mv.addObject(WEBSITES, taggedWebsites.asJava)

    val relatedTagLinks = relatedTagsService.getRelatedLinksForTag(tag, 8)
    if (relatedTagLinks.nonEmpty) {
      mv.addObject("related_tags", relatedTagLinks)
    }
    val relatedPublisherLinks = relatedTagsService.getRelatedPublishersForTag(tag, 8)
    if (relatedPublisherLinks.nonEmpty) {
      mv.addObject("related_publishers", relatedPublisherLinks)
    }
    populateCommentedTaggedNewsitems(mv, tag)
    populateGeocoded(mv, tag)
    import scala.collection.JavaConverters._
    mv.addObject(TAG_WATCHLIST, contentRetrievalService.getTagWatchlist(tag).asJava)
    mv.addObject(TAG_FEEDS, contentRetrievalService.getTaggedFeeds(tag).asJava)
    mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5, 1).asJava)
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
    tags(0)
  }

}
