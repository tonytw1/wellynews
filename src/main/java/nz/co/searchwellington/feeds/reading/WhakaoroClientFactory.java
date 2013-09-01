package nz.co.searchwellington.feeds.reading;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.http.HttpBadRequestException;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.eelpieconsulting.common.http.HttpForbiddenException;
import uk.co.eelpieconsulting.common.http.HttpNotFoundException;
import uk.co.eelpieconsulting.whakaoro.client.WhakaoroClient;
import uk.co.eelpieconsulting.whakaoro.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.whakaoro.client.model.Subscription;

@Component
public class WhakaoroClientFactory {

	private static Logger log = Logger.getLogger(WhakaoroClientFactory.class);
	
	private String url;
	private final String channel;
	
	@Autowired
	public WhakaoroClientFactory(@Value("#{config['whakaoko.url']}") String url, @Value("#{config['whakaoko.channel']}") String channel) {
		this.url = url;
		this.channel = channel;
	}
	
	public String createFeedSubscription(String url) {
		log.info("Requesting Whakakaoro subscription for feed");	
		final WhakaoroClient whakaoroClient = getClient();
		try {
			Subscription createdFeedSubscription = whakaoroClient.createFeedSubscription(channel, url);
			return createdFeedSubscription.getId();
			
		} catch (UnsupportedEncodingException e) {
			log.error(e);
		} catch (HttpNotFoundException e) {
			log.error(e);
		} catch (HttpBadRequestException e) {
			log.error(e);
		} catch (HttpForbiddenException e) {
			log.error(e);
		} catch (HttpFetchException e) {
			log.error(e);
		} catch (ParsingException e) {
			log.error(e);
		}
		return null;
	}

	public WhakaoroClient getClient() {
		return new WhakaoroClient(url);
	}
	
	public String getChannel() {
		return channel;
	}
	
}
