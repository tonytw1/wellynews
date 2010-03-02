 package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class ViewFeedModelBuilder extends AbstractModelBuilder implements ModelBuilder {

	Logger log = Logger.getLogger(ViewFeedModelBuilder.class);
    	
	private RssfeedNewsitemService rssfeedNewsitemService;
	private ContentRetrievalService contentRetrievalService;
	
	 
	public ViewFeedModelBuilder(RssfeedNewsitemService rssfeedNewsitemService, ContentRetrievalService contentRetrievalService) {
		this.rssfeedNewsitemService = rssfeedNewsitemService;
		this.contentRetrievalService = contentRetrievalService;
	}
	
	
	public boolean isValid(HttpServletRequest request) {
		return request.getAttribute("feedAttribute") != null;
	}

	
	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		if (isValid(request)) {
			log.info("Building view feed model");
			Feed feed = (Feed) request.getAttribute("feedAttribute");
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
		       
		       mv.setViewName("viewfeed");
		       return mv;            
			}
		}
		return null;
	}

	
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {
		populateSecondaryFeeds(mv);
	}
	
	
	// TODO duplication with BaseM'E'C
	public void populateSecondaryFeeds(ModelAndView mv) {      
        mv.addObject("righthand_heading", "Local Feeds");                
        mv.addObject("righthand_description", "Recently updated feeds from local organisations.");        
        final List<Resource> allFeeds = contentRetrievalService.getAllFeeds();
        if (allFeeds.size() > 0) {
            mv.addObject("righthand_content", allFeeds);       
        }
    }

	
}
