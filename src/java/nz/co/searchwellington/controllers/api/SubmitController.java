package nz.co.searchwellington.controllers.api;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.admin.AdminRequestFilter;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

public class SubmitController extends MultiActionController {

	Logger log = Logger.getLogger(SubmitController.class);
	private ResourceRepository resourceDAO;
	private AdminRequestFilter requestFilter;
	private ApiKeyAuthenticator keyAuthenticator;
        

    public SubmitController(ResourceRepository resourceDAO, AdminRequestFilter requestFilter, ApiKeyAuthenticator keyAuthenticator) {		
		this.resourceDAO = resourceDAO;
		this.requestFilter = requestFilter;
		this.keyAuthenticator = keyAuthenticator;
	}

    
    public ModelAndView tag(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        if (request.getParameter("key") != null && keyAuthenticator.isAuthentic(request.getParameter("key"))) {        
        	requestFilter.loadAttributesOntoRequest(request);        
        	final String resourceUrl = request.getParameter("url");
        	final Tag tag = (Tag) request.getAttribute("tag");        
        	if (resourceUrl != null && tag != null) {
        		Resource resource = resourceDAO.loadResourceByUniqueUrl(resourceUrl);
        		if (resource != null) {
        			resource.addTag(tag);
        			log.info("Applied tag: " + tag.getDisplayName() + " to resource: " + resource.getName());
        			resourceDAO.saveResource(resource);
        			mv.setViewName("apiResponseOK");
        			return mv;
        		} else {
        			log.info("No unique resource found for url: " + resourceUrl);
        		}
        	} else {
        		log.info("No resource url or valid tag found");
        	}
        } else {
        	log.info("no valid key");
        }
		mv.setViewName("apiResponseERROR");
        return mv; 		
    }

}
