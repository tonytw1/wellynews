package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.models.{GeotaggedNewsitemExtractor, ModelBuilder}
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, RssfeedNewsitemService}
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem
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

    def populateGeotaggedFeedItems(mv: ModelAndView, feedNewsitems: Seq[FrontendFeedNewsitem]) {
      val geotaggedItems = geotaggedNewsitemExtractor.extractGeotaggedItemsFromFeedNewsitems(feedNewsitems)
      if (!geotaggedItems.isEmpty) {
        mv.addObject("geocoded", geotaggedItems)
      }
    }

    def populateFeedItems(mv: ModelAndView, feed: Feed) {
      val feedNewsitems = rssfeedNewsitemService.getFeedNewsitems(feed)
      if (feedNewsitems != null && !feedNewsitems.isEmpty) {
        mv.addObject(MAIN_CONTENT, feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(feedNewsitems))
        populateGeotaggedFeedItems(mv, feedNewsitems)
      }
    }

    if (isValid(request)) {
      val feed = request.getAttribute(FEED_ATTRIBUTE).asInstanceOf[Feed]
      if (feed != null) {
        val mv = new ModelAndView
        mv.addObject("feed", frontendResourceMapper.createFrontendResourceFrom(feed))
        commonAttributesModelBuilder.setRss(mv, feed.getName, feed.getUrl)
        populateFeedItems(mv, feed)
        Some(mv)
      } else {
        None
      }

    } else {
      None
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    commonAttributesModelBuilder.populateSecondaryFeeds(mv)
  }

  def getViewName(mv: ModelAndView): String = {
    return "viewfeed"
  }

}
