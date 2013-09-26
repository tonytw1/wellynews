 package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.feeds.FeedItemLocalCopyDecorator;
import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;
import nz.co.searchwellington.model.mappers.FrontendResourceMapper;
import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

@Component
public class FeedModelBuilder extends AbstractModelBuilder implements ModelBuilder {
	
	private static final String FEED_ATTRIBUTE = "feedAttribute";
	
	private RssfeedNewsitemService rssfeedNewsitemService;
	private GeotaggedNewsitemExtractor geotaggedNewsitemExtractor;
	private FeedItemLocalCopyDecorator feedItemLocalCopyDecorator;
	private FrontendResourceMapper frontendResourceMapper;
		
	@Autowired
	public FeedModelBuilder(RssfeedNewsitemService rssfeedNewsitemService,
			ContentRetrievalService contentRetrievalService,
			GeotaggedNewsitemExtractor geotaggedNewsitemExtractor,
			FeedItemLocalCopyDecorator feedItemLocalCopyDecorator, 
			FrontendResourceMapper frontendResourceMapper) {
		this.rssfeedNewsitemService = rssfeedNewsitemService;
		this.contentRetrievalService = contentRetrievalService;
		this.geotaggedNewsitemExtractor = geotaggedNewsitemExtractor;
		this.feedItemLocalCopyDecorator = feedItemLocalCopyDecorator;
		this.frontendResourceMapper = frontendResourceMapper;
	}

	@Override
	public boolean isValid(HttpServletRequest request) {
		return request.getAttribute(FEED_ATTRIBUTE) != null;
	}
	
	@Override
	public ModelAndView populateContentModel(HttpServletRequest request) {
		if (isValid(request)) {
			Feed feed = (Feed) request.getAttribute(FEED_ATTRIBUTE);
			if (feed != null) {
				final ModelAndView mv = new ModelAndView();
				mv.addObject("feed", frontendResourceMapper.createFrontendResourceFrom(feed));			
				setRss(mv, feed.getName(), feed.getUrl());
				populateFeedItems(mv, feed);
				return mv;
			}
		}
		return null;
	}
	
	@Override
	public void populateExtraModelContent(HttpServletRequest request, ModelAndView mv) {
		populateSecondaryFeeds(mv);
	}
	
	@Override
	public String getViewName(ModelAndView mv) {
		return "viewfeed";
	}
	
	private void populateFeedItems(ModelAndView mv, Feed feed) {
		final List<FrontendFeedNewsitem> feedNewsitems = rssfeedNewsitemService.getFeedNewsitems(feed);		
		if (feedNewsitems != null && !feedNewsitems.isEmpty()) {
			mv.addObject("main_content", feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(feedNewsitems));
			populateGeotaggedFeedItems(mv, feedNewsitems);			
		}
	}
	
	private void populateGeotaggedFeedItems(ModelAndView mv, List<FrontendFeedNewsitem> feedNewsitems) {
		final List<FrontendNewsitem> geotaggedItems = geotaggedNewsitemExtractor.extractGeotaggedItemsFromFeedNewsitems(feedNewsitems);
		if (!geotaggedItems.isEmpty()) {
			mv.addObject("geocoded", geotaggedItems);
		}		
	}
	
}
