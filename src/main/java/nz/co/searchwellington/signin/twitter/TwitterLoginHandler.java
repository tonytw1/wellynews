package nz.co.searchwellington.signin.twitter;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.HibernateBackedUserDAO;
import nz.co.searchwellington.signin.SigninHandler;
import nz.co.searchwellington.twitter.TwitterApiFactory;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import com.google.common.collect.Maps;

@Component
public class TwitterLoginHandler implements SigninHandler {

	private static Logger log = Logger.getLogger(TwitterLoginHandler.class);
	
	private HibernateBackedUserDAO userDAO;
	private TwitterApiFactory twitterApiFactory;
	private Map<String, RequestToken> tokens;
	
	@Autowired
	public TwitterLoginHandler(HibernateBackedUserDAO userDAO, TwitterApiFactory twitterApiFactory) {
		this.userDAO = userDAO;
		this.twitterApiFactory = twitterApiFactory;
		this.tokens = Maps.newConcurrentMap();
	}
	
	@Override
	public ModelAndView getLoginView(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (!twitterApiFactory.apiIsConfigured()) {
			log.warn("Twitter API is not configured - not attempting to get send user to twitter");
			return null;
		}
		
		try {
			log.info("Getting request token");
			
			final Twitter twitterApi = twitterApiFactory.getTwitterApi();
			
			final RequestToken requestToken = twitterApi.getOAuthRequestToken();
			log.info("Got request token: " + requestToken.getToken());
			tokens.put(requestToken.getToken(), requestToken);
				
			final String authorizeUrl = requestToken.getAuthenticationURL();
			log.info("Redirecting user to authorize url : " + authorizeUrl);
			return new ModelAndView(new RedirectView(authorizeUrl));			
			
		} catch (Exception e) {
			log.warn("Failed to obtain request token.", e);
		}
		return null;	
	}
	
	@Override
	public Object getExternalUserIdentifierFromCallbackRequest(HttpServletRequest request) {
		if (!twitterApiFactory.apiIsConfigured()) {
			log.warn("Twitter API is not configured - not attempting to get external user");
			return null;
		}
		
		if (request.getParameter("oauth_token") != null && request.getParameter("oauth_verifier") != null) {
			final String token = request.getParameter("oauth_token");
			final String verifier = request.getParameter("oauth_verifier");

			log.info("oauth_token: " + token);
			log.info("oauth_verifier: " + verifier);
			
			log.info("Looking for request token: " + token);
			RequestToken requestToken = tokens.get(token);
			if (requestToken != null) {
				log.info("Found stored request token: " + requestToken.getToken());
				
				try {
					log.debug("Exchanging request token for access token");				
					AccessToken accessToken = twitterApiFactory.getTwitterApi().getOAuthAccessToken(requestToken, verifier);

					if (accessToken != null) {
						log.info("Got access token: '" + accessToken.getToken() + "', '" + accessToken.getTokenSecret() + "'");
						tokens.remove(requestToken.getToken());
						
						log.info("Using access token to lookup twitter user details");
						twitter4j.User twitterUser = getTwitteUserCredentials(accessToken);
						if (twitterUser != null) {
							log.info("Authenticated user is: " + twitterUser.getName());
							return twitterUser;
							
						} else {
							log.warn("Failed up obtain twitter user details");
						}
					}
					
				} catch (TwitterException e) {
					log.error(e);
					log.warn("Could not get access token for: " + requestToken.getToken());
					throw new RuntimeException(e);
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
	public User getUserByExternalIdentifier(Object externalIdentifier) {
		twitter4j.User twitterUser = (twitter4j.User) externalIdentifier;
		return userDAO.getUserByTwitterId(twitterUser.getId());
	}
	
	@Override
	public void decorateUserWithExternalSigninIdentifier(User user, Object externalIdentifier) {		
		twitter4j.User twitterUser = (twitter4j.User) externalIdentifier;
		if (user.getProfilename() == null || user.isUnlinkedAccount()) {
			final String twitterScreenName = twitterUser.getScreenName();
			if (userDAO.getUserByProfileName(twitterScreenName) == null) {
				user.setProfilename(twitterScreenName);
			}
		}
		user.setTwitterId(twitterUser.getId());
	}
		
	private twitter4j.User getTwitteUserCredentials(AccessToken accessToken) {
		Twitter twitterApi = twitterApiFactory.getOauthedTwitterApiForAccessToken(accessToken.getToken(), accessToken.getTokenSecret());
		try {
			return twitterApi.verifyCredentials();
		} catch (TwitterException e) {
			log.warn("Failed up obtain twitter user details due to Twitter exception: " + e.getMessage());
			return null;
		}
	}
	
}
