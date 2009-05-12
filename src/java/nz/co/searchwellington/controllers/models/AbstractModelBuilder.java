package nz.co.searchwellington.controllers.models;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.ModelAndView;

public abstract class AbstractModelBuilder {
	
	protected static final int MAX_NUMBER_OF_GEOTAGGED_TO_SHOW = 30;
	protected static final int MAX_NUMBER_OF_COMMENTED_TO_SHOW = 500;
	protected static final int MAX_WEBSITES = 500;
	
	
	protected static final int MAX_NEWSITEMS = 30;
	protected static final int MAX_NUMBER_OF_COMMENTED_TO_SHOW_IN_RHS = 2;
	
	
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
	
	
	protected void populatePagination(ModelAndView mv, int startIndex, int totalNewsitemCount) {
		mv.addObject("main_content_total", totalNewsitemCount);
		mv.addObject("max_page_number", ((totalNewsitemCount / 30) + 1));
		
		int endIndex = startIndex + MAX_NEWSITEMS > totalNewsitemCount ? totalNewsitemCount : startIndex + MAX_NEWSITEMS;		
		mv.addObject("start_index", startIndex + 1);
		mv.addObject("end_index", endIndex);
	}
	
}