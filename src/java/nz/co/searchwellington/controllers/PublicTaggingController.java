package nz.co.searchwellington.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.admin.AdminRequestFilter;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.User;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

public class PublicTaggingController extends BaseMultiActionController {

	private AdminRequestFilter adminRequestFilter;
	private AnonUserService anonUserService;
	private SubmissionProcessingService submissionProcessingService;
	
	
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
		setUser(request, newUser);
		loggedInUserFilter.loadLoggedInUser(request);
		return newUser;
	}
    
    private void setUser(HttpServletRequest request, User user) {
		request.getSession().setAttribute("user", user);		
	}
}
