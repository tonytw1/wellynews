package nz.co.searchwellington.controllers.ajax;

import java.util.List;

import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;


public class PublisherAjaxController extends BaseAjaxController {
	
    static Logger log = Logger.getLogger(PublisherAjaxController.class);
	private ResourceRepository resourceDAO;

   
    public PublisherAjaxController(ResourceRepository resourceDAO) {
		this.resourceDAO = resourceDAO;
	}
    	
    protected List<String> getSuggestions(String q) {
        log.info("Looking up possible publishers starting with: " + q);
        return resourceDAO.getPublisherNamesByStartingLetters(q);    
	}
    
}
    