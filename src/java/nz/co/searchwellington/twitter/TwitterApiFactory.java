package nz.co.searchwellington.twitter;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.http.AccessToken;

public class TwitterApiFactory {
	
	private String consumerKey;
	private String consumerSecret;
	
	private	String username;
    private String password;
	
	public Twitter getOauthedTwitterApiForAccessToken(String token, String secret) {
		ConfigurationBuilder configBuilder = new ConfigurationBuilder().setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret);
		Twitter twitterApi = new TwitterFactory(configBuilder.build()).getOAuthAuthorizedInstance(new AccessToken(token, secret));
		return twitterApi;
	}
	
	@Deprecated
	public Twitter getHttpAuthTwitterApi() {
    	return new TwitterFactory().getInstance(username, password);
	}
	
	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}
	
	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
    public void setPassword(String password) {
        this.password = password;
    }
    
	public boolean isConfigured() {
		return this.username != null && !this.username.equals("") && this.password != null && !this.password.equals("");
	}
	
}
