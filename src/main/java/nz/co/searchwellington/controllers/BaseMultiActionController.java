package nz.co.searchwellington.controllers;

import nz.co.searchwellington.repositories.ConfigDAO;
import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.springframework.web.servlet.ModelAndView;

@Deprecated
public abstract class BaseMultiActionController {	// TODO not a great pattern
	
    final protected int MAX_SECONDARY_ITEMS = 4;
    final protected int MAX_NEWSITEMS = 30;
    final protected int MAX_EVENTS_TO_SHOW_ON_FRONT = 10;
    
    protected UrlStack urlStack;
    protected ConfigDAO configDAO;
    protected LoggedInUserFilter loggedInUserFilter;
    protected ContentRetrievalService contentRetrievalService;
    
    // TODO migrate all inlines to use this.
    final protected void populateCommonLocal(ModelAndView mv) {      
        mv.addObject("top_level_tags", contentRetrievalService.getTopLevelTags());      
    }
    
    final protected void populateSecondaryLatestNewsitems(ModelAndView mv) {        
    }
        
}
