package nz.co.searchwellington.controllers.ajax;

import java.util.List;

import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.apache.log4j.Logger;


public class PublisherAjaxController extends BaseAjaxController {
	
    private static Logger log = Logger.getLogger(PublisherAjaxController.class);
	
    private ContentRetrievalService contentRetrievalService;
		
    public PublisherAjaxController(ContentRetrievalService contentRetrievalService) {
		this.contentRetrievalService = contentRetrievalService;
	}
    
	protected List<String> getSuggestions(String q) {
        log.info("Looking up possible publishers starting with: " + q);
        return contentRetrievalService.getPublisherNamesByStartingLetters(q);    
	}
    
}
