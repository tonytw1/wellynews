package nz.co.searchwellington.openid;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.AnonUserService;
import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.controllers.LoginResourceOwnershipService;
import nz.co.searchwellington.controllers.UrlStack;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.UserRepository;

import org.apache.log4j.Logger;
import org.scribe.oauth.Scribe;
import org.scribe.oauth.Token;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;

public class TwitterLoginController extends MultiActionController {

	static Logger log = Logger.getLogger(TwitterLoginController.class);
	
	private OAuthScribeFactory scribeFactory;
	private UserRepository userDAO;
	private LoggedInUserFilter loggedInUserFilter;
	private AnonUserService anonUserService;
	private LoginResourceOwnershipService loginResourceOwnershipService;
	private UrlStack urlStack;

	private Map<String, Token> tokens;

	
	public TwitterLoginController(OAuthScribeFactory scribeFactory,
			UserRepository userDAO, LoggedInUserFilter loggedInUserFilter,
			AnonUserService anonUserService,
			LoginResourceOwnershipService loginResourceOwnershipService,
			UrlStack urlStack) {
		this.scribeFactory = scribeFactory;
		this.userDAO = userDAO;
		this.loggedInUserFilter = loggedInUserFilter;
		this.anonUserService = anonUserService;
		this.loginResourceOwnershipService = loginResourceOwnershipService;
		this.urlStack = urlStack;
		this.tokens = new HashMap<String, Token>();
	}
	
	
	public ModelAndView login(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Scribe scribe = scribeFactory.getScribe();
		
		try {
			Token requestToken = scribe.getRequestToken();
			if (requestToken != null) {
				log.info("Got request token: " + requestToken.getToken());
				tokens.put(requestToken.getToken(), requestToken);
				
				final String authorizeUrl = "http://api.twitter.com/oauth/authenticate?oauth_token=" + requestToken.getToken();				
				RedirectView redirectView = new RedirectView(authorizeUrl);
				log.info("Redirecting user to: " + redirectView.getUrl());
				return new ModelAndView(redirectView);
			}
		} catch (Exception e) {
			log.error(e);
		}
		
		// TODO error screen
		return null;		
	}
	
	
	@Transactional
	public ModelAndView callback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (request.getParameter("oauth_token") != null && request.getParameter("oauth_verifier") != null) {
			Scribe scribe = scribeFactory.getScribe();
			final String token = request.getParameter("oauth_token");
			final String verifier = request.getParameter("oauth_verifier");

			log.info("Looking for request token: " + token);
			Token requestToken = tokens.get(token);
			if (requestToken != null) {
				
				Token accessToken = scribe.getAccessToken(requestToken, verifier);
				if (accessToken != null) {
					log.info("Got access token: " + accessToken);
				
					Twitter twitterApi = new TwitterFactory().getOAuthAuthorizedInstance(new AccessToken(accessToken.getToken(), accessToken.getSecret()));
					twitter4j.User twitterUser = twitterApi.verifyCredentials();
					if (twitterUser != null) {
						String username = twitterUser.getScreenName();
						log.info("Twitter user is: " + username);
												
						User user = userDAO.getUserByTwitterName(username);			
						if (user == null) {
							
							User loggedInUser = loggedInUserFilter.getLoggedInUser();				
							// No existing user for this identity.				
							if (loggedInUser == null) {
								log.info("Creating new user for openid username: " + username);
								user = createNewUser(username);
								
								
							} else {
								user = loggedInUser;
								log.info("Attaching verified username to user: " + username);
								loggedInUser.setUsername(username);					
							}
							
						}			
						
						if (user != null) {
							log.info("Setting logged in user to: " + user.getName());				
							User loggedInUser = loggedInUserFilter.getLoggedInUser();				
							if (loggedInUser != null) {
								log.info("Reassigning resource ownership from " + loggedInUser.getProfilename() + " to " + user.getProfilename());
								loginResourceOwnershipService.reassignOwnership(loggedInUser, user);
							}
							setUser(request, user);
							
						} else {
							log.warn("User was null after successful openid auth");
						}				
					    return new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)));						
					}
					
					
				} else {
					log.warn("Could not get access token for: " + requestToken.getToken());
						
				}
				 
				
				
			} else {
				log.warn("Could not find request token for: " + token);
			}
			
		} else {
			log.error("oauth token or verifier missing from callback request");
		}
		return null;
	}
	
	
	private User createNewUser(final String username) {
		User newUser = anonUserService.createAnonUser();
		newUser.setProfilename(username);
		userDAO.saveUser(newUser);
		log.info("Created new user with username: " + newUser.getUsername());
		return newUser;
	}

	
	// TODO duplicated with ResourceEditController
	private void setUser(HttpServletRequest request, User user) {
		request.getSession().setAttribute("user", user);	
	}
	
	
	
}
