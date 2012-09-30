package nz.co.searchwellington.signin.twitter;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.HibernateBackedUserDAO;
import nz.co.searchwellington.signin.SigninHandler;
import nz.co.searchwellington.twitter.TwitterApiFactory;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import twitter4j.Twitter;
import twitter4j.TwitterException;

@Component
public class TwitterLoginHandler implements SigninHandler {

	private static Logger log = Logger.getLogger(TwitterLoginHandler.class);
	
	private HibernateBackedUserDAO userDAO;
	private TwitterApiFactory twitterApiFactory;
	private Map<String, Token> tokens;
	private UrlBuilder urlBuilder;
	
    @Value("#{config['consumer.key']}")
	private String consumerKey;
    
    @Value("#{config['consumer.secret']}")
	private String consumerSecret;

	private OAuthService oauthService;	
	
	@Autowired
	public TwitterLoginHandler(HibernateBackedUserDAO userDAO, TwitterApiFactory twitterApiFactory, UrlBuilder urlBuilder) {
		this.userDAO = userDAO;
		this.twitterApiFactory = twitterApiFactory;
		this.tokens = new HashMap<String, Token>();
		this.urlBuilder = urlBuilder;
	}
	
	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}
	
	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}
	
	@Override
	public ModelAndView getLoginView(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (!apiIsConfigured()) {
			log.warn("Twitter API is not configured - not attempting to get send user to twitter");
			return null;
		}
		
		try {
			log.info("Getting request token");			
			OAuthService service = getOauthService();
			
			Token requestToken = service.getRequestToken();		
			if (requestToken != null) {
				log.info("Got request token: " + requestToken.getToken());
				tokens.put(requestToken.getToken(), requestToken);
				
				final String authorizeUrl = service.getAuthorizationUrl(requestToken);
				log.info("Redirecting user to authorize url : " + authorizeUrl);
				RedirectView redirectView = new RedirectView(authorizeUrl);
				return new ModelAndView(redirectView);
			}
			
		} catch (Exception e) {
			log.warn("Failed to obtain request token.", e);
		}
		return null;	
	}
	
	@Override
	public Object getExternalUserIdentifierFromCallbackRequest(HttpServletRequest request) {
		if (!apiIsConfigured()) {
			log.warn("Twitter API is not configured - not attempting to get external user");
			return null;
		}
		
		if (request.getParameter("oauth_token") != null && request.getParameter("oauth_verifier") != null) {
			final String token = request.getParameter("oauth_token");
			final String verifier = request.getParameter("oauth_verifier");

			log.info("oauth_token: " + token);
			log.info("oauth_verifier: " + verifier);
			
			log.info("Looking for request token: " + token);
			Token requestToken = tokens.get(token);
			if (requestToken != null) {
				log.info("Found stored request token: " + requestToken.getToken());
				
				log.debug("Exchanging request token for access token");
				
				OAuthService service = getOauthService();
				Token accessToken = service.getAccessToken(requestToken, new Verifier(verifier));
				
				if (accessToken != null) {
					log.info("Got access token: '" + accessToken.getToken() + "', '" + accessToken.getSecret() + "'");
					tokens.remove(requestToken.getToken());
					log.debug("Using access token to lookup twitter user details");
					twitter4j.User twitterUser = getTwitteUserCredentials(accessToken);
					if (twitterUser != null) {
						return twitterUser;
					} else {
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
	public User getUserByExternalIdentifier(Object externalIdentifier) {
		twitter4j.User twitterUser = (twitter4j.User) externalIdentifier;
		return userDAO.getUserByTwitterId(twitterUser.getId());
	}
	
	@Override
	public void decorateUserWithExternalSigninIdentifier(User user, Object externalIdentifier) {		
		twitter4j.User twitterUser = (twitter4j.User) externalIdentifier;
		if (user.getProfilename() == null || user.isUnlinkedAnonAccount()) {
			final String twitterScreenName = twitterUser.getScreenName();
			if (userDAO.getUserByProfileName(twitterScreenName) == null) {
				user.setProfilename(twitterScreenName);
			}
		}
		user.setTwitterId(twitterUser.getId());
	}
		
	private twitter4j.User getTwitteUserCredentials(Token accessToken) {
		Twitter twitterApi = twitterApiFactory.getOauthedTwitterApiForAccessToken(accessToken.getToken(), accessToken.getSecret());
		try {
			return twitterApi.verifyCredentials();
		} catch (TwitterException e) {
			log.warn("Failed up obtain twitter user details due to Twitter exception: " + e.getMessage());
			return null;
		}
	}
	
	private OAuthService getOauthService() {
		if (oauthService == null) {
			log.info("Building oauth service with consumer key and consumer secret: " + consumerKey + ":" + consumerSecret);
			oauthService = new ServiceBuilder().provider(new TwitterApi()).apiKey(consumerKey).apiSecret(consumerSecret).callback(urlBuilder.getTwitterCallbackUrl()).build();
		}
		return oauthService;
	}
	
	private boolean apiIsConfigured() {
		return consumerKey != null && !consumerKey.isEmpty() && consumerSecret != null && !consumerKey.isEmpty();
	}
	
}
