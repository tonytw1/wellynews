package nz.co.searchwellington.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.admin.AdminRequestFilter;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;


public class PublicTaggingController extends MultiActionController {
	
    static Logger log = Logger.getLogger(PublicTaggingController.class);
        
    private AdminRequestFilter adminRequestFilter;
	private TaggingReturnsOfficerService taggingReturnsOfficerService;
	private ContentRetrievalService contentRetrievalService;
	
	
    public PublicTaggingController(AdminRequestFilter adminRequestFilter,
			TaggingReturnsOfficerService taggingReturnsOfficerService, ContentRetrievalService contentRetrievalService) {
		this.adminRequestFilter = adminRequestFilter;
		this.taggingReturnsOfficerService = taggingReturnsOfficerService;
		this.contentRetrievalService = contentRetrievalService;
	}


	@Transactional
    public ModelAndView tagging(HttpServletRequest request, HttpServletResponse response) throws IOException {    	
    	adminRequestFilter.loadAttributesOntoRequest(request);    	
    	
    	Resource editResource = (Resource) request.getAttribute("resource");    	
    	if (request.getAttribute("resource") != null) {    		
    		ModelAndView mv = new ModelAndView("taggingVotes");
    		mv.addObject("top_level_tags", contentRetrievalService.getTopLevelTags());
    		
    		mv.addObject("heading", "Tagging votes");    		
            mv.addObject("resource", editResource);
            mv.addObject("votes", taggingReturnsOfficerService.complieTaggingVotes(editResource));
            
    		mv.addObject("top_level_tags", contentRetrievalService.getTopLevelTags());
    		mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5));
    		mv.addObject("latest_newsitems_moreurl", "index#newslog");
            return mv;
        }
    	
    	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return null;  	
    }
    
}
