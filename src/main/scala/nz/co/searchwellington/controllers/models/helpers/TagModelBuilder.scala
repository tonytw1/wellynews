package nz.co.searchwellington.controllers.models.helpers

import java.util.List
import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.controllers.RelatedTagsService
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.controllers.models.helpers.CommonAttributesModelBuilder
import nz.co.searchwellington.feeds.FeedItemLocalCopyDecorator
import nz.co.searchwellington.feeds.RssfeedNewsitemService
import nz.co.searchwellington.flickr.FlickrService
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.model.PublisherContentCount
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.model.TagContentCount
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import nz.co.searchwellington.urls.UrlParameterEncoder
import nz.co.searchwellington.views.GeocodeToPlaceMapper
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

@Component object TagModelBuilder {
  private var log: Logger = Logger.getLogger(classOf[TagModelBuilder])
  private val MAIN_CONTENT: String = "main_content"
  private val PAGE: String = "page"
  private val TAG: String = "tag"
  private val TAGS: String = "tags"
  private val TAG_WATCHLIST: String = "tag_watchlist"
  private val TAG_FEEDS: String = "tag_feeds"
  private val WEBSITES: String = "websites"
}

@Component class TagModelBuilder extends ModelBuilder {
  private var rssUrlBuilder: RssUrlBuilder = null
  private var urlBuilder: UrlBuilder = null
  private var relatedTagsService: RelatedTagsService = null
  private var rssfeedNewsitemService: RssfeedNewsitemService = null
  private var contentRetrievalService: ContentRetrievalService = null
  private var flickrService: FlickrService = null
  private var feedItemLocalCopyDecorator: FeedItemLocalCopyDecorator = null
  private var geocodeToPlaceMapper: GeocodeToPlaceMapper = null
  private var commonAttributesModelBuilder: CommonAttributesModelBuilder = null

  @Autowired def this(rssUrlBuilder: RssUrlBuilder, urlBuilder: UrlBuilder, relatedTagsService: RelatedTagsService, rssfeedNewsitemService: RssfeedNewsitemService, contentRetrievalService: ContentRetrievalService, flickrService: FlickrService, feedItemLocalCopyDecorator: FeedItemLocalCopyDecorator, geocodeToPlaceMapper: GeocodeToPlaceMapper, commonAttributesModelBuilder: CommonAttributesModelBuilder) {
    this()
    this.rssUrlBuilder = rssUrlBuilder
    this.urlBuilder = urlBuilder
    this.relatedTagsService = relatedTagsService
    this.rssfeedNewsitemService = rssfeedNewsitemService
    this.contentRetrievalService = contentRetrievalService
    this.flickrService = flickrService
    this.feedItemLocalCopyDecorator = feedItemLocalCopyDecorator
    this.geocodeToPlaceMapper = geocodeToPlaceMapper
    this.commonAttributesModelBuilder = commonAttributesModelBuilder
  }

  @SuppressWarnings(Array("unchecked")) def isValid(request: HttpServletRequest): Boolean = {
    val tags: List[Tag] = request.getAttribute(TagModelBuilder.TAGS).asInstanceOf[List[Tag]]
    return tags != null && tags.size == 1
  }

  @SuppressWarnings(Array("unchecked")) def populateContentModel(request: HttpServletRequest): ModelAndView = {
    if (isValid(request)) {
      val tags: List[Tag] = request.getAttribute(TagModelBuilder.TAGS).asInstanceOf[List[Tag]]
      val tag: Tag = tags.get(0)
      val page: Int = commonAttributesModelBuilder.getPage(request)
      return populateTagPageModelAndView(tag, page)
    }
    return null
  }

  @SuppressWarnings(Array("unchecked")) def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    val tags: List[Tag] = request.getAttribute(TagModelBuilder.TAGS).asInstanceOf[List[Tag]]
    val tag: Tag = tags.get(0)
    val taggedWebsites: List[FrontendResource] = contentRetrievalService.getTaggedWebsites(tag, CommonAttributesModelBuilder.MAX_WEBSITES)
    mv.addObject(TagModelBuilder.WEBSITES, taggedWebsites)
    val relatedTagLinks: List[TagContentCount] = relatedTagsService.getRelatedLinksForTag(tag, 8)
    if (!relatedTagLinks.isEmpty) {
      mv.addObject("related_tags", relatedTagLinks)
    }
    val relatedPublisherLinks: List[PublisherContentCount] = relatedTagsService.getRelatedPublishersForTag(tag, 8)
    if (!relatedPublisherLinks.isEmpty) {
      mv.addObject("related_publishers", relatedPublisherLinks)
    }
    populateCommentedTaggedNewsitems(mv, tag)
    populateRelatedFeed(mv, tag)
    populateGeocoded(mv, tag)
    populateTagFlickrPool(mv, tag)
    mv.addObject(TagModelBuilder.TAG_WATCHLIST, contentRetrievalService.getTagWatchlist(tag))
    mv.addObject(TagModelBuilder.TAG_FEEDS, contentRetrievalService.getTaggedFeeds(tag))
    mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5))
  }

  @SuppressWarnings(Array("unchecked")) def getViewName(mv: ModelAndView): String = {
    val mainContent: List[Resource] = mv.getModel.get(TagModelBuilder.MAIN_CONTENT).asInstanceOf[List[Resource]]
    val taggedWebsites: List[Resource] = mv.getModel.get(TagModelBuilder.WEBSITES).asInstanceOf[List[Resource]]
    val tagWatchlist: List[Resource] = mv.getModel.get(TagModelBuilder.TAG_WATCHLIST).asInstanceOf[List[Resource]]
    val tagFeeds: List[Resource] = mv.getModel.get(TagModelBuilder.TAG_FEEDS).asInstanceOf[List[Resource]]
    val hasSecondaryContent: Boolean = !taggedWebsites.isEmpty || !tagWatchlist.isEmpty || !tagFeeds.isEmpty
    val isOneContentType: Boolean = mainContent.isEmpty || !hasSecondaryContent
    val page: Integer = mv.getModel.get(TagModelBuilder.PAGE).asInstanceOf[Integer]
    if (page != null && page > 0) {
      mv.addObject(TagModelBuilder.PAGE, page)
      return "tagNewsArchive"
    }
    else if (isOneContentType) {
      return "tagOneContentType"
    }
    return "tag"
  }

  private def populateTagPageModelAndView(tag: Tag, page: Int): ModelAndView = {
    val mv: ModelAndView = new ModelAndView
    mv.addObject(TagModelBuilder.PAGE, page)
    val startIndex: Int = commonAttributesModelBuilder.getStartIndex(page)
    val totalNewsitemCount: Long = contentRetrievalService.getTaggedNewitemsCount(tag)
    if (startIndex > totalNewsitemCount) {
      return null
    }
    mv.addObject(TagModelBuilder.TAG, tag)
    if (tag.getGeocode != null) {
      mv.addObject("location", geocodeToPlaceMapper.mapGeocodeToPlace(tag.getGeocode))
    }
    mv.addObject("heading", tag.getDisplayName)
    mv.addObject("description", rssUrlBuilder.getRssDescriptionForTag(tag))
    mv.addObject("link", urlBuilder.getTagUrl(tag))
    val taggedNewsitems: List[FrontendResource] = contentRetrievalService.getTaggedNewsitems(tag, startIndex, CommonAttributesModelBuilder.MAX_NEWSITEMS)
    mv.addObject(TagModelBuilder.MAIN_CONTENT, taggedNewsitems)
    commonAttributesModelBuilder.populatePagination(mv, startIndex, totalNewsitemCount)
    if (taggedNewsitems.size > 0) {
      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForTag(tag), rssUrlBuilder.getRssUrlForTag(tag))
    }
    return mv
  }

  private def populateTagFlickrPool(mv: ModelAndView, tag: Tag) {
    mv.addObject("flickr_count", flickrService.getFlickrPhotoCountFor(tag))
    mv.addObject("escaped_flickr_group_id", UrlParameterEncoder.encode(flickrService.getPoolId))
  }

  private def populateCommentedTaggedNewsitems(mv: ModelAndView, tag: Tag) {
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

  private def populateGeocoded(mv: ModelAndView, tag: Tag) {
    val geocoded: List[FrontendResource] = contentRetrievalService.getTaggedGeotaggedNewsitems(tag, CommonAttributesModelBuilder.MAX_NUMBER_OF_GEOTAGGED_TO_SHOW)
    TagModelBuilder.log.debug("Found " + geocoded.size + " valid geocoded resources for tag: " + tag.getName)
    if (geocoded.size > 0) {
      mv.addObject("geocoded", geocoded)
    }
  }

  private def populateRelatedFeed(mv: ModelAndView, tag: Tag) {
    val relatedFeed: Feed = tag.getRelatedFeed
    if (relatedFeed != null) {
      TagModelBuilder.log.debug("Related feed is: " + relatedFeed.getName)
      mv.addObject("related_feed", relatedFeed)
      val relatedFeedItems: List[FrontendFeedNewsitem] = rssfeedNewsitemService.getFeedNewsitems(relatedFeed)
      mv.addObject("related_feed_items", feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(relatedFeedItems))
    }
    else {
      TagModelBuilder.log.debug("No related feed.")
    }
  }
}