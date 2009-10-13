package nz.co.searchwellington.controllers.api;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.SubmissionProcessingService;
import nz.co.searchwellington.controllers.admin.AdminRequestFilter;
import nz.co.searchwellington.model.PublishedResource;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

public class SubmitController extends MultiActionController {

	Logger log = Logger.getLogger(SubmitController.class);
	private ResourceRepository resourceDAO;
	private AdminRequestFilter adminRequestFilter;
	private ApiKeyAuthenticator keyAuthenticator;
	private SubmissionProcessingService submissionProcessingService;
        

    public SubmitController(ResourceRepository resourceDAO, AdminRequestFilter adminRequestFilter, ApiKeyAuthenticator keyAuthenticator, SubmissionProcessingService submissionProcessingService) {		
		this.resourceDAO = resourceDAO;
		this.adminRequestFilter = adminRequestFilter;
		this.keyAuthenticator = keyAuthenticator;
		this.submissionProcessingService = submissionProcessingService;
	}
    
    
    
    // TODO process publisher
    @Transactional
    public ModelAndView save(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
    	ModelAndView mv = new ModelAndView();
    	request.setCharacterEncoding("UTF-8");
    	
    	log.info("Accepting newsitem from api call");
        adminRequestFilter.loadAttributesOntoRequest(request);
        
        Resource editResource = resourceDAO.createNewFeed();
    	boolean resourceUrlHasChanged = submissionProcessingService.processUrl(request, editResource);
         
    	submissionProcessingService.processTitle(request, editResource);
    	log.info("Calling geocode");
    	submissionProcessingService.processGeocode(request, editResource);
    	submissionProcessingService.processDate(request, editResource);
    	submissionProcessingService.processDescription(request, editResource);
    	submissionProcessingService.processTags(request, editResource);
         
    	if (editResource.getType().equals("N")) {
    		//	processImage(request, (Newsitem) editResource, loggedInUser);            
    	}
         
    	// Set publisher field.
    	boolean isPublishedResource = editResource instanceof PublishedResource;
    	if (isPublishedResource) {
    		((PublishedResource) editResource).setPublisher((Website) request.getAttribute("publisher"));           
    	}
    	
    	log.info("Saving api submitted newsitem: " + editResource.getName());
    	saveResource(editResource); 
    	log.info("Id after save is: " + editResource.getId());
    	mv.setViewName("apiResponseOK");
    	return mv;      
     }
    
    
	private void saveResource(Resource editResource) {
		resourceDAO.saveResource(editResource);		
	}

	
}
