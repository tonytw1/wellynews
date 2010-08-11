package nz.co.searchwellington.openid;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.AnonUserService;
import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.controllers.LoginResourceOwnershipService;
import nz.co.searchwellington.controllers.UrlStack;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.UserRepository;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;

public abstract class AbstractExternalSigninController extends MultiActionController {

	static Logger log = Logger.getLogger(AbstractExternalSigninController.class);
	
	protected LoggedInUserFilter loggedInUserFilter;
	protected UserRepository userDAO;
	protected AnonUserService anonUserService;
	protected LoginResourceOwnershipService loginResourceOwnershipService;
	protected UrlStack urlStack;
	
	abstract protected Object getExternalUserIdentifierFromCallbackRequest(HttpServletRequest request);
	abstract protected User getUserByExternalIdentifier(Object externalIdentifier);
	abstract protected void decorateUserWithExternalSigninIndenfier(User user, Object externalIdentifier);
	
	@Transactional
	final public ModelAndView callback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		final Object externalIdentifier = getExternalUserIdentifierFromCallbackRequest(request);
		if (externalIdentifier != null) {			
			log.info("External user is: " + externalIdentifier.toString());
			User user = getUserByExternalIdentifier(externalIdentifier);
			if (user == null) {
				User loggedInUser = loggedInUserFilter.getLoggedInUser();				
				// No existing user for this identity.				
				if (loggedInUser == null) {
					user = createNewUser(externalIdentifier);
					
				} else {
					user = loggedInUser;
					log.info("Attaching external identifier to current user: " + externalIdentifier.toString());
					decorateUserWithExternalSigninIndenfier(loggedInUser, externalIdentifier.toString());
				}
			}
						
			if (user != null) {	// User should always be not null here, in theory
				User loggedInUser = loggedInUserFilter.getLoggedInUser();				
				if (loggedInUser != null) {
					log.info("Reassigning resource ownership from " + loggedInUser.getProfilename() + " to " + user.getProfilename());
					loginResourceOwnershipService.reassignOwnership(loggedInUser, user);
				}
				setUser(request, user);
				
			} else {
				log.warn("User was null after successful external signin");
			}			
			return new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)));						
		}
		
		return signinErrorView(request);
	}
	
	
	protected ModelAndView signinErrorView(HttpServletRequest request) {
		return new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)));	// TODO replace with something meaningful
	}
	
	
	final protected User createNewUser(Object externalIdentifier) {
		User newUser = anonUserService.createAnonUser();
		decorateUserWithExternalSigninIndenfier(newUser, externalIdentifier);
		userDAO.saveUser(newUser);
		log.info("Created new user with external identifier: " + externalIdentifier.toString());
		return newUser;
	}
	
	// TODO duplicated with ResourceEditController
	final protected void setUser(HttpServletRequest request, User user) {
		request.getSession().setAttribute("user", user);	
	}
	
	
}
