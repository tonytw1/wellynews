package nz.co.searchwellington.signin;

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

public class SigninController extends MultiActionController {

	static Logger log = Logger.getLogger(SigninController.class);
	
	protected LoggedInUserFilter loggedInUserFilter;
	protected UserRepository userDAO;
	protected AnonUserService anonUserService;
	protected LoginResourceOwnershipService loginResourceOwnershipService;
	protected UrlStack urlStack;
	protected SigninHandler signinHandler;
	
	
	public SigninController(
			LoggedInUserFilter loggedInUserFilter, UserRepository userDAO,
			AnonUserService anonUserService,
			LoginResourceOwnershipService loginResourceOwnershipService,
			UrlStack urlStack, SigninHandler signinHandler) {

		this.loggedInUserFilter = loggedInUserFilter;
		this.userDAO = userDAO;
		this.anonUserService = anonUserService;
		this.loginResourceOwnershipService = loginResourceOwnershipService;
		this.urlStack = urlStack;
		this.signinHandler = signinHandler;
	}

	
	public ModelAndView login(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView loginView = signinHandler.getLoginView(request, response);
		if (loginView != null) {
			return loginView;
		}
		return signinErrorView(request);		
	}
	
	
	
	@Transactional
	public ModelAndView callback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		final Object externalIdentifier = signinHandler.getExternalUserIdentifierFromCallbackRequest(request);
		if (externalIdentifier != null) {
			log.info("External user is: " + externalIdentifier.toString());
			
			User user = signinHandler.getUserByExternalIdentifier(externalIdentifier);
			if (user == null) {
				User loggedInUser = loggedInUserFilter.getLoggedInUser();				
				// No existing user for this identity.
				if (loggedInUser == null) {
					user = createNewUser(externalIdentifier);
					
				} else {
					user = loggedInUser;
					log.info("Attaching external identifier to current user: " + externalIdentifier.toString());
					signinHandler.decorateUserWithExternalSigninIndenfier(loggedInUser, externalIdentifier.toString());
				}
			}
			
			User alreadyLoggedInUser = loggedInUserFilter.getLoggedInUser();		
			if (alreadyLoggedInUser != null) {
				
				if (alreadyLoggedInUser.isUnlinkedAnonAccount()) {	// TODO coverage
					if (!user.equals(alreadyLoggedInUser)) {
						log.info("Reassigning resource ownership from " + alreadyLoggedInUser.getProfilename() + " to " + user.getProfilename());
						loginResourceOwnershipService.reassignOwnership(alreadyLoggedInUser, user);
						userDAO.deleteUser(alreadyLoggedInUser);
					}
				}
				
			}
				
			loggedInUserFilter.setLoggedInUser(request, user);			
			return new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)));						
		}
		
		return signinErrorView(request);
	}
	
	protected ModelAndView signinErrorView(HttpServletRequest request) {
		return new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)));	// TODO replace with something meaningful
	}
	
	
	final protected User createNewUser(Object externalIdentifier) {
		User newUser = anonUserService.createAnonUser();
		signinHandler.decorateUserWithExternalSigninIndenfier(newUser, externalIdentifier);
		userDAO.saveUser(newUser);
		log.info("Created new user with external identifier: " + externalIdentifier.toString());
		return newUser;
	}
	
}
