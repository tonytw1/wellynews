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

	static Logger log = Logger.getLogger(ViewFeedModelBuilder.class);
    	
	private RssfeedNewsitemService rssfeedNewsitemService;
	private ContentRetrievalService contentRetrievalService;
	
	 
	public ViewFeedModelBuilder(RssfeedNewsitemService rssfeedNewsitemService, ContentRetrievalService contentRetrievalService) {
		this.rssfeedNewsitemService = rssfeedNewsitemService;
		this.contentRetrievalService = contentRetrievalService;
	}
	
	
	@Override
	public boolean isValid(HttpServletRequest request) {
		return request.getAttribute("feedAttribute") != null;
	}

	
	@Override
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

	
	
	// TODO duplication with BaseM'E'C
	public void populateSecondaryFeeds(ModelAndView mv) {      
        mv.addObject("righthand_heading", "Local Feeds");                
        mv.addObject("righthand_description", "Recently updated feeds from local organisations.");        
        final List<Resource> allFeeds = contentRetrievalService.getAllFeedsOrderByLatestItemDate();
        if (allFeeds.size() > 0) {
            mv.addObject("righthand_content", allFeeds);       
        }
    }
	
}
