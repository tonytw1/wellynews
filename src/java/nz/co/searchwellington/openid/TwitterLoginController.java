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
import org.springframework.web.servlet.view.RedirectView;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;

public class TwitterLoginController extends AbstractExternalSigninController {

	private static final String OAUTH_AUTHEN_URL = "http://api.twitter.com/oauth/authenticate?oauth_token=";

	static Logger log = Logger.getLogger(TwitterLoginController.class);
	
	private OAuthScribeFactory scribeFactory;
	private UserRepository userDAO;
	private LoggedInUserFilter loggedInUserFilter;
	private AnonUserService anonUserService;
	private LoginResourceOwnershipService loginResourceOwnershipService;
	private UrlStack urlStack;

	private Map<String, Token> tokens;
	private Scribe scribe;
	
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
		scribe = scribeFactory.getScribe();
	}
	
	
	public ModelAndView login(HttpServletRequest request, HttpServletResponse response) throws Exception {		
		try {
			log.info("Getting request token");
			Token requestToken = scribe.getRequestToken();
			if (requestToken != null) {
				log.info("Got request token: " + requestToken.getToken());
				tokens.put(requestToken.getToken(), requestToken);
				
				final String authorizeUrl = OAUTH_AUTHEN_URL + requestToken.getToken();				
				RedirectView redirectView = new RedirectView(authorizeUrl);
				log.info("Redirecting user to: " + redirectView.getUrl());
				return new ModelAndView(redirectView);
			}
			
		} catch (Exception e) {
			log.warn("Failed to obtain request token" + e.getMessage());
		}
		
		// TODO error screen
		return null;		
	}
	
	
	@Transactional
	public ModelAndView callback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Integer twitterId = (Integer) getExternalUserIdentifierFromCallbackRequest(request);
		if (twitterId != null) {
			
			log.info("Twitter user is: " + twitterId);
			User user = userDAO.getUserByTwitterId(twitterId);
			if (user == null) {
				User loggedInUser = loggedInUserFilter.getLoggedInUser();				
				// No existing user for this identity.				
				if (loggedInUser == null) {
					log.info("Creating new user for twitter users: " + twitterId);
					user = createNewUser(twitterId);
				} else {
					user = loggedInUser;
					log.info("Attaching external identifier to current user: " + twitterId);
					decorateUserWithExternalSigninIndenfier(loggedInUser, twitterId);
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
	
		return null;	// TODO correct fail
	}


	@Override
	protected Integer getExternalUserIdentifierFromCallbackRequest(HttpServletRequest request) {
		Integer twitterId = null;
		if (request.getParameter("oauth_token") != null && request.getParameter("oauth_verifier") != null) {
			final String token = request.getParameter("oauth_token");
			final String verifier = request.getParameter("oauth_verifier");

			log.info("Looking for request token: " + token);
			Token requestToken = tokens.get(token);
			if (requestToken != null) {
				log.debug("Found stored request token: " + requestToken.getToken());
				
				log.debug("Exchanging for access token");
				Token accessToken = scribe.getAccessToken(requestToken, verifier);
				if (accessToken != null) {
					log.debug("Got access token: " + accessToken);
					
					log.debug("Using access token to lookup twitter user details");
					Twitter twitterApi = new TwitterFactory().getOAuthAuthorizedInstance(new AccessToken(accessToken.getToken(), accessToken.getSecret()));
					twitter4j.User twitterUser;
					try {
						twitterUser = twitterApi.verifyCredentials();
						if (twitterUser != null) {
							twitterId = twitterUser.getId();
						} else {
							log.warn("Failed up obtain twitter user details");
						}
					} catch (TwitterException e) {
						log.warn("Failed up obtain twitter user details");
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
		return twitterId;
	}
	
	
	private User createNewUser(final int twitterId) {
		User newUser = anonUserService.createAnonUser();
		decorateUserWithExternalSigninIndenfier(newUser, twitterId);
		userDAO.saveUser(newUser);	// TODO generic
		log.info("Created new user with username: " + newUser.getOpenId());
		return newUser;
	}

	@Override
	protected void decorateUserWithExternalSigninIndenfier(User user, Object externalIdentifier) {
		int twitterId = (Integer) externalIdentifier;
		user.setTwitterId(twitterId);
	}
	

	// TODO duplicated with ResourceEditController
	private void setUser(HttpServletRequest request, User user) {
		request.getSession().setAttribute("user", user);	
	}
	
}
