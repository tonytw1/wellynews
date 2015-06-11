package nz.co.searchwellington.controllers.models.helpers

import java.util.List
import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.controllers.models.helpers.CommonAttributesModelBuilder
import nz.co.searchwellington.feeds.FeedItemLocalCopyDecorator
import nz.co.searchwellington.feeds.RssfeedNewsitemService
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem
import nz.co.searchwellington.model.frontend.FrontendNewsitem
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

@Component object FeedModelBuilder {
  private val FEED_ATTRIBUTE: String = "feedAttribute"
}

@Component class FeedModelBuilder extends ModelBuilder {
  private var rssfeedNewsitemService: RssfeedNewsitemService = null
  private var geotaggedNewsitemExtractor: GeotaggedNewsitemExtractor = null
  private var feedItemLocalCopyDecorator: FeedItemLocalCopyDecorator = null
  private var frontendResourceMapper: FrontendResourceMapper = null
  private var contentRetrievalService: ContentRetrievalService = null
  private var commonAttributesModelBuilder: CommonAttributesModelBuilder = null

  @Autowired def this(rssfeedNewsitemService: RssfeedNewsitemService, contentRetrievalService: ContentRetrievalService, geotaggedNewsitemExtractor: GeotaggedNewsitemExtractor, feedItemLocalCopyDecorator: FeedItemLocalCopyDecorator, frontendResourceMapper: FrontendResourceMapper, commonAttributesModelBuilder: CommonAttributesModelBuilder) {
    this()
    this.rssfeedNewsitemService = rssfeedNewsitemService
    this.contentRetrievalService = contentRetrievalService
    this.geotaggedNewsitemExtractor = geotaggedNewsitemExtractor
    this.feedItemLocalCopyDecorator = feedItemLocalCopyDecorator
    this.frontendResourceMapper = frontendResourceMapper
    this.commonAttributesModelBuilder = commonAttributesModelBuilder
  }

  def isValid(request: HttpServletRequest): Boolean = {
    return request.getAttribute(FeedModelBuilder.FEED_ATTRIBUTE) != null
  }

  def populateContentModel(request: HttpServletRequest): ModelAndView = {
    if (isValid(request)) {
      val feed: Feed = request.getAttribute(FeedModelBuilder.FEED_ATTRIBUTE).asInstanceOf[Feed]
      if (feed != null) {
        val mv: ModelAndView = new ModelAndView
        mv.addObject("feed", frontendResourceMapper.createFrontendResourceFrom(feed))
        commonAttributesModelBuilder.setRss(mv, feed.getName, feed.getUrl)
        populateFeedItems(mv, feed)
        return mv
      }
    }
    return null
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    commonAttributesModelBuilder.populateSecondaryFeeds(mv)
  }

  def getViewName(mv: ModelAndView): String = {
    return "viewfeed"
  }

  private def populateFeedItems(mv: ModelAndView, feed: Feed) {
    val feedNewsitems: List[FrontendFeedNewsitem] = rssfeedNewsitemService.getFeedNewsitems(feed)
    if (feedNewsitems != null && !feedNewsitems.isEmpty) {
      mv.addObject("main_content", feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(feedNewsitems))
      populateGeotaggedFeedItems(mv, feedNewsitems)
    }
  }

  private def populateGeotaggedFeedItems(mv: ModelAndView, feedNewsitems: List[FrontendFeedNewsitem]) {
    val geotaggedItems: List[FrontendNewsitem] = geotaggedNewsitemExtractor.extractGeotaggedItemsFromFeedNewsitems(feedNewsitems)
    if (!geotaggedItems.isEmpty) {
      mv.addObject("geocoded", geotaggedItems)
    }
  }
}