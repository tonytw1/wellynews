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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;

public class TwitterLoginController extends AbstractExternalSigninController {

	private static final String OAUTH_AUTHEN_URL = "http://api.twitter.com/oauth/authenticate?oauth_token=";
	static Logger log = Logger.getLogger(TwitterLoginController.class);
	
	@SuppressWarnings("unused")
	private OAuthScribeFactory scribeFactory;
	
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
		
		return signinErrorView(request);	
	}
	
	
	@Override
	protected Object getExternalUserIdentifierFromCallbackRequest(HttpServletRequest request) {
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
					tokens.remove(requestToken.getToken());
					
					log.debug("Using access token to lookup twitter user details");
					Twitter twitterApi = new TwitterFactory().getOAuthAuthorizedInstance(new AccessToken(accessToken.getToken(), accessToken.getSecret()));
					try {
						twitter4j.User twitterUser = twitterApi.verifyCredentials();
						if (twitterUser != null) {
							return twitterUser;
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
		return null;
	}
	
	
	@Override
	protected User getUserByExternalIdentifier(Object externalIdentifier) {
		twitter4j.User twitterUser = (twitter4j.User) externalIdentifier;
		return userDAO.getUserByTwitterId(twitterUser.getId());
	}

		
	@Override
	protected void decorateUserWithExternalSigninIndenfier(User user, Object externalIdentifier) {
		twitter4j.User twitterUser = (twitter4j.User) externalIdentifier;
		if (user.getProfilename() == null || user.isUnlinkedAnonAccount()) {
			final String twitterScreenName = twitterUser.getScreenName();
			if (userDAO.getUserByProfileName(twitterScreenName) == null) {
				user.setProfilename(twitterScreenName);
			}
		}
		user.setTwitterId(twitterUser.getId());
	}
	
}
