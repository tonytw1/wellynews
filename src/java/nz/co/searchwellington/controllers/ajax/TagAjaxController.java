package nz.co.searchwellington.controllers.ajax;

import java.util.List;

import nz.co.searchwellington.repositories.TagDAO;

import org.apache.log4j.Logger;


public class TagAjaxController extends BaseAjaxController {
	
    static Logger log = Logger.getLogger(TagAjaxController.class);
	private TagDAO tagDAO;
	
	public TagAjaxController(TagDAO tagDAO) {
		this.tagDAO = tagDAO;
	}
	
    protected List<String> getSuggestions(String q) {
        log.info("Looking up possible tags starting with: " + q);
        return tagDAO.getTagNamesStartingWith(q);
	}
        
}
    