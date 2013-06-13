package nz.co.searchwellington.twitter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

@Component
public class TwitterApiFactory {
	
    @Value("#{config['consumer.key']}")
	private String consumerKey;
    
    @Value("#{config['consumer.secret']}")
	private String consumerSecret;
    
    @Value("#{config['access.token']}")
	private String accessToken;

    @Value("#{config['access.secret']}")
	private String accessSecret;
    
	public Twitter getOauthedTwitterApiForAccessToken(String token, String secret) {
		ConfigurationBuilder configBuilder = new ConfigurationBuilder().setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret);
		return new TwitterFactory(configBuilder.build()).getInstance(new AccessToken(token, secret));
	}
	
	public Twitter getOauthedTwitterApi() {
		ConfigurationBuilder configBuilder = new ConfigurationBuilder().setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret);
		return new TwitterFactory(configBuilder.build()).getInstance(new AccessToken(accessToken, accessSecret));
	}
	
}
