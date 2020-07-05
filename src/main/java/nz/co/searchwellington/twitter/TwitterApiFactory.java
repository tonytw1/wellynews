package nz.co.searchwellington.twitter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import com.google.common.base.Strings;

@Component
public class TwitterApiFactory {

  @Value("${consumer.key}")
  private String consumerKey;

  @Value("${consumer.secret}")
  private String consumerSecret;

  public Twitter getOauthedTwitterApiForAccessToken(String token, String secret) {
    return new TwitterFactory(buildConfig()).getInstance(new AccessToken(token, secret));
  }

  public Twitter getTwitterApi() {
    return new TwitterFactory(buildConfig()).getInstance();
  }

  public boolean apiIsConfigured() {
    return !Strings.isNullOrEmpty(consumerKey) && !Strings.isNullOrEmpty(consumerSecret);
  }

  private Configuration buildConfig() {
    return new ConfigurationBuilder().setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret).build();
  }

}