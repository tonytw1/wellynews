package nz.co.searchwellington.twitter;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.http.AccessToken;

public class TwitterApiFactory {
	
	private String consumerKey;
	private String consumerSecret;
	
	private String accessToken;
	private String accessSecret;
	
	public Twitter getOauthedTwitterApiForAccessToken(String token, String secret) {
		ConfigurationBuilder configBuilder = new ConfigurationBuilder().setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret);
		return new TwitterFactory(configBuilder.build()).getOAuthAuthorizedInstance(new AccessToken(token, secret));
	}
	
	public Twitter getOauthedTwitterApi() {
		ConfigurationBuilder configBuilder = new ConfigurationBuilder().setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret);
		return new TwitterFactory(configBuilder.build()).getOAuthAuthorizedInstance(new AccessToken(accessToken, accessSecret));
	}
	
	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}
	
	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public void setAccessSecret(String accessSecret) {
		this.accessSecret = accessSecret;
	}
	
}
