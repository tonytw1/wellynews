 package nz.co.searchwellington.controllers.models;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class FeedModelBuilder extends AbstractModelBuilder implements ModelBuilder {

	static Logger log = Logger.getLogger(FeedModelBuilder.class);
	
	private static final String FEED_ATTRIBUTE = "feedAttribute";
	static Pattern feedPattern = Pattern.compile("^/feed/(.*)$");
	
	private RssfeedNewsitemService rssfeedNewsitemService;
	private ContentRetrievalService contentRetrievalService;
	
	
	public FeedModelBuilder(RssfeedNewsitemService rssfeedNewsitemService, ContentRetrievalService contentRetrievalService) {
		this.rssfeedNewsitemService = rssfeedNewsitemService;
		this.contentRetrievalService = contentRetrievalService;
	}
	
	
	@Override
	public boolean isValid(HttpServletRequest request) {
		populateFeed(request);
		return request.getAttribute(FEED_ATTRIBUTE) != null;
	}

	
	@Override
	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		if (isValid(request)) {
			log.info("Building view feed model");
			Feed feed = (Feed) request.getAttribute(FEED_ATTRIBUTE);
			if (feed != null) {
				ModelAndView mv = new ModelAndView();
				mv.addObject("feed", feed);
				
				List<FeedNewsitem> feedNewsitems = rssfeedNewsitemService.getFeedNewsitems(feed);
				rssfeedNewsitemService.addSupressionAndLocalCopyInformation(feedNewsitems);
				
				if (feedNewsitems != null && feedNewsitems.size() > 0) {
					mv.addObject("main_content", feedNewsitems);
				} else {
					log.warn("No newsitems were loaded from feed: " + feed.getName());
				}
				
				setRss(mv, feed.getName(), feed.getUrl());		       
				return mv;
			}
		}
		return null;
	}

	
	@Override
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {
		populateSecondaryFeeds(mv);
	}
	
	
	@Override
	public String getViewName(ModelAndView mv) {
		return "viewfeed";
	}

	
	private void populateFeed(HttpServletRequest request) {
		Matcher feedMatcher = feedPattern.matcher(request.getPathInfo());
		if (feedMatcher.matches()) {
			final String feedUrlWords = feedMatcher.group(1);
			Feed feed = contentRetrievalService.getFeedByUrlWord(feedUrlWords);
			if (feed != null) {
				request.setAttribute(FEED_ATTRIBUTE, feed);
			}
		}
	}
	
}
