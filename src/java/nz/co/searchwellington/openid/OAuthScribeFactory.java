package nz.co.searchwellington.openid;

import java.util.Properties;

import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.scribe.oauth.Scribe;

public class OAuthScribeFactory {
	
	static Logger log = Logger.getLogger(OAuthScribeFactory.class);
	
	private UrlBuilder urlBuilder;
	private String consumerKey;
	private String consumerSecret;

	public OAuthScribeFactory(UrlBuilder urlBuilder) {
		this.urlBuilder = urlBuilder;
	}
	
	public Scribe getScribe() {
		Properties props = new Properties();
		props.setProperty("consumer.key", consumerKey);
		props.setProperty("consumer.secret", consumerSecret);
		props.setProperty("request.token.verb", "GET");
		props.setProperty("request.token.url", "http://api.twitter.com/oauth/request_token");
		props.setProperty("access.token.verb", "GET");
		props.setProperty("access.token.url", "http://api.twitter.com/oauth/access_token");
		props.setProperty("callback.url", urlBuilder.getTwitterCallbackUrl());	
		props.setProperty("scribe.equalizer", "org.scribe.eq.LinkedInEqualizer");
		return new Scribe(props);
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}
	
}
