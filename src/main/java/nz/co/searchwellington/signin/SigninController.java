package nz.co.searchwellington.signin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.AnonUserService;
import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.controllers.LoginResourceOwnershipService;
import nz.co.searchwellington.controllers.UrlStack;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.HibernateBackedUserDAO;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class SigninController {

	private static Logger log = Logger.getLogger(SigninController.class);
	
	private LoggedInUserFilter loggedInUserFilter;
	private HibernateBackedUserDAO userDAO;
	private AnonUserService anonUserService;
	private LoginResourceOwnershipService loginResourceOwnershipService;
	private UrlStack urlStack;
	private SigninHandler signinHandler;
	
	public SigninController() {
	}
	
	public SigninController(
			LoggedInUserFilter loggedInUserFilter, HibernateBackedUserDAO userDAO,
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
	
	@RequestMapping("/twitter/login")
	public ModelAndView login(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView loginView = signinHandler.getLoginView(request, response);
		if (loginView != null) {
			return loginView;
		}
		return signinErrorView(request);		
	}
	
	@Transactional
	@RequestMapping("/twitter/callback")
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
					signinHandler.decorateUserWithExternalSigninIdentifier(loggedInUser, externalIdentifier.toString());
				}
			}
			
			User alreadyLoggedInUser = loggedInUserFilter.getLoggedInUser();		
			if (alreadyLoggedInUser != null) {
				
				if (alreadyLoggedInUser.isUnlinkedAnonAccount()) {	// TODO coverage
					if (!user.equals(alreadyLoggedInUser)) {
						log.info("Reassigning resource ownership from " + alreadyLoggedInUser.getProfilename() + " to " + user.getProfilename());
						loginResourceOwnershipService.reassignOwnership(alreadyLoggedInUser, user);
					}
				}
				
			}
				
			loggedInUserFilter.setLoggedInUser(request, user);			
			return new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)));						
		}
		
		return signinErrorView(request);
	}
	
	private ModelAndView signinErrorView(HttpServletRequest request) {
		return new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)));	// TODO replace with something meaningful
	}
		
	private User createNewUser(Object externalIdentifier) {
		User newUser = anonUserService.createAnonUser();
		signinHandler.decorateUserWithExternalSigninIdentifier(newUser, externalIdentifier);
		userDAO.saveUser(newUser);
		log.info("Created new user with external identifier: " + externalIdentifier.toString());
		return newUser;
	}
	
}
