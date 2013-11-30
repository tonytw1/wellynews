package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

@Component
public abstract class AbstractModelBuilder {
	
	protected static final int MAX_NUMBER_OF_GEOTAGGED_TO_SHOW = 30;
	protected static final int MAX_NUMBER_OF_COMMENTED_TO_SHOW = 30;
	protected static final int MAX_WEBSITES = 500;
	
	protected static final int MAX_NEWSITEMS = 30;
	protected static final int MAX_NUMBER_OF_COMMENTED_TO_SHOW_IN_RHS = 2;
	
	protected ContentRetrievalService contentRetrievalService;
	
	protected int getPage(HttpServletRequest request) {
		int page = 0;
		if (request.getAttribute("page") != null) {
			page = (Integer) request.getAttribute("page");		
		}
		return page;
	}
	
	protected int getStartIndex(int page) {
		int startIndex = 0;
		if (page > 1) {
			startIndex = (page -1 ) * MAX_NEWSITEMS;			
		}
		return startIndex;
	}
	
	protected void setRss(ModelAndView mv, String title, String url) {
		mv.addObject("rss_title", title);
		mv.addObject("rss_url", url);
	}
	
	protected void populatePagination(ModelAndView mv, int startIndex, long totalNewsitemCount) {
		mv.addObject("main_content_total", totalNewsitemCount);
		mv.addObject("max_page_number", ((totalNewsitemCount / 30) + 1));
		
		long endIndex = startIndex + MAX_NEWSITEMS > totalNewsitemCount ? totalNewsitemCount : startIndex + MAX_NEWSITEMS;		
		mv.addObject("start_index", startIndex + 1);
		mv.addObject("end_index", endIndex);
	}
	
	protected final void populateSecondaryFeeds(ModelAndView mv) {	// TODO should be latest news items by default?  
        mv.addObject("righthand_heading", "Local Feeds");                
        mv.addObject("righthand_description", "Recently updated feeds from local organisations.");        
        final List<FrontendResource> allFeeds = contentRetrievalService.getAllFeedsOrderByLatestItemDate();
        if (allFeeds != null && allFeeds.size() > 0) {
            mv.addObject("righthand_content", allFeeds);       
        }
    }
	
}