package nz.co.searchwellington.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.admin.AdminRequestFilter;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.User;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class PublicTaggingController {
	
    private static Logger log = Logger.getLogger(PublicTaggingController.class);
    
	private AdminRequestFilter adminRequestFilter;
	private AnonUserService anonUserService;
	private SubmissionProcessingService submissionProcessingService;
	private LoggedInUserFilter loggedInUserFilter;
	private UrlStack urlStack;
	
	public PublicTaggingController() {
	}
	
	@Autowired
	public PublicTaggingController(AdminRequestFilter adminRequestFilter,
			LoggedInUserFilter loggedInUserFilter,
			AnonUserService anonUserService, 
			SubmissionProcessingService submissionProcessingService, 
			UrlStack urlStack) {
		this.adminRequestFilter = adminRequestFilter;
		this.loggedInUserFilter = loggedInUserFilter;
		this.anonUserService = anonUserService;
		this.submissionProcessingService = submissionProcessingService;
		this.urlStack = urlStack;
	}
	
	@Transactional
	@RequestMapping(value="/tagging/submit", method=RequestMethod.POST)
    public ModelAndView tag(HttpServletRequest request, HttpServletResponse response) {	
		adminRequestFilter.loadAttributesOntoRequest(request);    	
    	Resource resource = (Resource) request.getAttribute("resource");    	
    	if (request.getAttribute("resource") == null) { 
    		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        	return null;
    	}
    	
    	User loggedInUser = loggedInUserFilter.getLoggedInUser();
    	if (loggedInUser == null) {
    		loggedInUser = createAndSetAnonUser(request);
    	}
    	
		submissionProcessingService.processTags(request, resource, loggedInUser);
		return new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)));	
	}
	
	private User createAndSetAnonUser(HttpServletRequest request) {
		log.info("Creating new anon user for resource submission");
		User newUser = anonUserService.createAnonUser();
		loggedInUserFilter.setLoggedInUser(request, newUser);
		loggedInUserFilter.loadLoggedInUser(request);
		return newUser;
	}
	
}
