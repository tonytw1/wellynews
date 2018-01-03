package nz.co.searchwellington.controllers.models.helpers

import java.util.List
import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.controllers.{RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, RssfeedNewsitemService}
import nz.co.searchwellington.model.{Feed, PublisherContentCount, Resource, Tag, TagContentCount}
import nz.co.searchwellington.model.frontend.{FrontendFeedNewsitem, FrontendResource}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.{UrlBuilder, UrlParameterEncoder}
import nz.co.searchwellington.views.GeocodeToPlaceMapper
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

@Component class TagModelBuilder @Autowired() (rssUrlBuilder: RssUrlBuilder, urlBuilder: UrlBuilder,
                                              relatedTagsService: RelatedTagsService, rssfeedNewsitemService: RssfeedNewsitemService,
                                              contentRetrievalService: ContentRetrievalService, feedItemLocalCopyDecorator: FeedItemLocalCopyDecorator,
                                              geocodeToPlaceMapper: GeocodeToPlaceMapper, commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder {

  private val log = Logger.getLogger(classOf[TagModelBuilder])

  private val MAIN_CONTENT = "main_content"
  private val PAGE = "page"
  private val TAG = "tag"
  private val TAGS = "tags"
  private val TAG_WATCHLIST = "tag_watchlist"
  private val TAG_FEEDS = "tag_feeds"
  private val WEBSITES = "websites"

  def isValid(request: HttpServletRequest): Boolean = {
    val tags: List[Tag] = request.getAttribute(TAGS).asInstanceOf[List[Tag]]
    return tags != null && tags.size == 1
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {

    def populateTagPageModelAndView(tag: Tag, page: Int): ModelAndView = {
      val mv = new ModelAndView
      mv.addObject(PAGE, page)
      val startIndex = commonAttributesModelBuilder.getStartIndex(page)
      val totalNewsitemCount = contentRetrievalService.getTaggedNewitemsCount(tag)
      if (startIndex > totalNewsitemCount) {
        None
      }
      mv.addObject(TAG, tag)
      if (tag.getGeocode != null) {
        mv.addObject("location", geocodeToPlaceMapper.mapGeocodeToPlace(tag.getGeocode))
      }
      mv.addObject("heading", tag.getDisplayName)
      mv.addObject("description", rssUrlBuilder.getRssDescriptionForTag(tag))
      mv.addObject("link", urlBuilder.getTagUrl(tag))

      val taggedNewsitems = contentRetrievalService.getTaggedNewsitems(tag, startIndex, CommonAttributesModelBuilder.MAX_NEWSITEMS)
      mv.addObject(MAIN_CONTENT, taggedNewsitems)
      commonAttributesModelBuilder.populatePagination(mv, startIndex, totalNewsitemCount)
      if (taggedNewsitems.size > 0) {
        commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForTag(tag), rssUrlBuilder.getRssUrlForTag(tag))
      }
      Some(mv)
    }

    if (isValid(request)) {
      val tags = request.getAttribute(TAGS).asInstanceOf[List[Tag]]
      val tag = tags.get(0)
      val page = commonAttributesModelBuilder.getPage(request)
      Some(populateTagPageModelAndView(tag, page))
    }

    None
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {

    def populateCommentedTaggedNewsitems(mv: ModelAndView, tag: Tag) {
      val recentCommentedNewsitems: List[FrontendResource] = contentRetrievalService.getRecentCommentedNewsitemsForTag(tag, CommonAttributesModelBuilder.MAX_NUMBER_OF_COMMENTED_TO_SHOW_IN_RHS + 1)
      var commentedToShow: List[FrontendResource] = null
      if (recentCommentedNewsitems.size <= CommonAttributesModelBuilder.MAX_NUMBER_OF_COMMENTED_TO_SHOW_IN_RHS) {
        commentedToShow = recentCommentedNewsitems
      }
      else {
        commentedToShow = recentCommentedNewsitems.subList(0, CommonAttributesModelBuilder.MAX_NUMBER_OF_COMMENTED_TO_SHOW_IN_RHS)
      }
      val commentsCount: Int = contentRetrievalService.getCommentedNewsitemsForTagCount(tag)
      val moreCommentCount: Int = commentsCount - commentedToShow.size
      if (moreCommentCount > 0) {
        mv.addObject("commented_newsitems_morecount", moreCommentCount)
        mv.addObject("commented_newsitems_moreurl", urlBuilder.getTagCommentUrl(tag))
      }
      mv.addObject("commented_newsitems", commentedToShow)
    }


    val tags: List[Tag] = request.getAttribute(TAGS).asInstanceOf[List[Tag]]
    val tag: Tag = tags.get(0)
    val taggedWebsites = contentRetrievalService.getTaggedWebsites(tag, CommonAttributesModelBuilder.MAX_WEBSITES)
    mv.addObject(WEBSITES, taggedWebsites)
    val relatedTagLinks = relatedTagsService.getRelatedLinksForTag(tag, 8)
    if (!relatedTagLinks.isEmpty) {
      mv.addObject("related_tags", relatedTagLinks)
    }
    val relatedPublisherLinks = relatedTagsService.getRelatedPublishersForTag(tag, 8)
    if (!relatedPublisherLinks.isEmpty) {
      mv.addObject("related_publishers", relatedPublisherLinks)
    }
    populateCommentedTaggedNewsitems(mv, tag)
    populateRelatedFeed(mv, tag)
    populateGeocoded(mv, tag)
    populateTagFlickrPool(mv, tag)
    mv.addObject(TAG_WATCHLIST, contentRetrievalService.getTagWatchlist(tag))
    mv.addObject(TAG_FEEDS, contentRetrievalService.getTaggedFeeds(tag))
    mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5, 1))
  }

  def getViewName(mv: ModelAndView): String = {
    val mainContent = mv.getModel.get(MAIN_CONTENT).asInstanceOf[List[Resource]]
    val taggedWebsites= mv.getModel.get (WEBSITES).asInstanceOf[List[Resource]]
    val tagWatchlist = mv.getModel.get(TAG_WATCHLIST).asInstanceOf[List[Resource]]
    val tagFeeds = mv.getModel.get(TAG_FEEDS).asInstanceOf[List[Resource]]
    val hasSecondaryContent = !taggedWebsites.isEmpty || !tagWatchlist.isEmpty || !tagFeeds.isEmpty
    val isOneContentType = mainContent.isEmpty || !hasSecondaryContent
    val page = mv.getModel.get(PAGE).asInstanceOf[Integer]
    if (page != null && page > 0) {
      mv.addObject(PAGE, page)
      return "tagNewsArchive"
    }
    else if (isOneContentType) {
      return "tagOneContentType"
    }
    return "tag"
  }

  private def populateGeocoded(mv: ModelAndView, tag: Tag) {
    val geocoded = contentRetrievalService.getTaggedGeotaggedNewsitems(tag, CommonAttributesModelBuilder.MAX_NUMBER_OF_GEOTAGGED_TO_SHOW)
    log.debug("Found " + geocoded.size + " valid geocoded resources for tag: " + tag.getName)
    if (geocoded.size > 0) {
      mv.addObject("geocoded", geocoded)
    }
  }

  private def populateRelatedFeed(mv: ModelAndView, tag: Tag) {
    val relatedFeed = tag.getRelatedFeed
    if (relatedFeed != null) {
      log.debug("Related feed is: " + relatedFeed.getName)
      mv.addObject("related_feed", relatedFeed)
      val relatedFeedItems = rssfeedNewsitemService.getFeedNewsitems(relatedFeed)
      mv.addObject("related_feed_items", feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(relatedFeedItems))
    }
    else {
      log.debug("No related feed.")
    }
  }

}