package nz.co.searchwellington.controllers;

import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

// TODO could be an interceptor?
@Component
public class CommonModelObjectsService {

	private final ContentRetrievalService contentRetrievalService;
	
	@Autowired
	public CommonModelObjectsService(ContentRetrievalService contentRetrievalService) {
		this.contentRetrievalService = contentRetrievalService;
	}
	
    public void populateCommonLocal(ModelAndView mv) {      
        mv.addObject("top_level_tags", contentRetrievalService.getTopLevelTags());      
    }
	
}
