package nz.co.searchwellington.controllers.models;

import org.springframework.web.servlet.ModelAndView;

public abstract class AbstractModelBuilder {
	
	protected static final int MAX_NUMBER_OF_GEOTAGGED_TO_SHOW = 30;
	protected static final int MAX_NUMBER_OF_COMMENTED_TO_SHOW = 500;
	protected static final int MAX_WEBSITES = 500;
	
	
	protected static final int MAX_NEWSITEMS = 30;
	protected static final int MAX_NUMBER_OF_COMMENTED_TO_SHOW_IN_RHS = 2;
	
	
	
	
	protected void setRss(ModelAndView mv, String title, String url) {
		mv.addObject("rss_title", title);
		mv.addObject("rss_url", url);
	}  
	
}