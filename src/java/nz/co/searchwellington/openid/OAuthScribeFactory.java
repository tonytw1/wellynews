package nz.co.searchwellington.openid;

import java.util.Properties;

import org.scribe.oauth.Scribe;

public class OAuthScribeFactory {

	public Scribe getScribe() {
		Properties props = new Properties();
		props.setProperty("consumer.key", "t2uvCBaNKtwLxJMuYhDg");
		props.setProperty("consumer.secret", "NwKpRUEDeYox4fyz6RBuLKNz18XCh1GBRBOdtFHpI");
		props.setProperty("request.token.verb", "GET");
		props.setProperty("request.token.url", "https://api.twitter.com/oauth/request_token");
		props.setProperty("access.token.verb", "POST");
		props.setProperty("access.token.url", "https://api.twitter.com/oauth/access_token");
		props.setProperty("callback.url", "http://localhost:8080/springapp/twitter/callback");
		props.setProperty("scribe.equalizer", "org.scribe.eq.LinkedInEqualizer");
		return new Scribe(props);
	}

}
