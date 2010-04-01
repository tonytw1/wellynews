package nz.co.searchwellington.controllers.ajax;

import java.util.List;

import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.apache.log4j.Logger;


public class TagAjaxController extends BaseAjaxController {
	
    static Logger log = Logger.getLogger(TagAjaxController.class);
	private ContentRetrievalService contentRetrievalService;
	
		
    public TagAjaxController(ContentRetrievalService contentRetrievalService) {
		this.contentRetrievalService = contentRetrievalService;
	}


	protected List<String> getSuggestions(String q) {
        log.info("Looking up possible tags starting with: " + q);
        return contentRetrievalService.getTagNamesStartingWith(q);
	}
        
}
