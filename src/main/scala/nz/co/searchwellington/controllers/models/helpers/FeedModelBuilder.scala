package nz.co.searchwellington.controllers.models.helpers

import java.util.List
import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.models.{GeotaggedNewsitemExtractor, ModelBuilder}
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, RssfeedNewsitemService}
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.model.frontend.{FrontendFeedNewsitem, FrontendNewsitem}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

@Component class FeedModelBuilder @Autowired()(rssfeedNewsitemService: RssfeedNewsitemService, contentRetrievalService: ContentRetrievalService, geotaggedNewsitemExtractor: GeotaggedNewsitemExtractor, feedItemLocalCopyDecorator: FeedItemLocalCopyDecorator, frontendResourceMapper: FrontendResourceMapper,
                                               commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder {

  private val FEED_ATTRIBUTE: String = "feedAttribute"

  def isValid(request: HttpServletRequest): Boolean = {
    return request.getAttribute(FEED_ATTRIBUTE) != null
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {
    if (isValid(request)) {
      val feed = request.getAttribute(FEED_ATTRIBUTE).asnstanceOf[Feed]
      if (feed != null) {
        val mv = new ModelAndView
        mv.addObject("feed", frontendResourceMapper.createFrontendResourceFrom(feed))
        commonAttributesModelBuilder.setRss(mv, feed.getName, feed.getUrl)
        populateFeedItems(mv, feed)
        Some(mv)
      }
    }
    None
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