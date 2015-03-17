package nz.co.searchwellington.feeds.reading;

import org.apache.log4j.Logger;
import org.elasticsearch.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.common.http.HttpBadRequestException;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.eelpieconsulting.common.http.HttpForbiddenException;
import uk.co.eelpieconsulting.common.http.HttpNotFoundException;
import uk.co.eelpieconsulting.whakaoro.client.WhakaoroClient;
import uk.co.eelpieconsulting.whakaoro.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem;
import uk.co.eelpieconsulting.whakaoro.client.model.Subscription;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Component
public class WhakaoroService {

	private static Logger log = Logger.getLogger(WhakaoroService.class);
	
	private final String url;
	private final String username;
	private final String channel;
	
	@Autowired
	public WhakaoroService(@Value("#{config['whakaoko.url']}") String url,
                           @Value("#{config['whakaoko.username']}") String username,
                           @Value("#{config['whakaoko.channel']}") String channel) {
		this.url = url;
		this.username = username;
		this.channel = channel;
	}
	
	public String createFeedSubscription(String url) {
		log.info("Requesting Whakakaoro subscription for feed");	
		final WhakaoroClient whakaoroClient = getClient();
		try {
			final Subscription createdFeedSubscription = whakaoroClient.createFeedSubscription(username, channel, url);
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
	
	public List<FeedItem> getSubscriptionFeedItems(String subscriptionId) {
        try {
    		return getClient().getSubscriptionFeedItems(username, subscriptionId);

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
        return Lists.newArrayList();
	}
	
	public List<FeedItem> getChannelFeedItems() {
        try {
    		List<FeedItem> channelFeedItems = Lists.newArrayList();
	    	for (int page = 0; page <= 5; page++) {
		    	channelFeedItems.addAll(getClient().getChannelFeedItems(username, channel, page));
	    	}
		    return channelFeedItems;

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
        return Lists.newArrayList();
	}
	
	private WhakaoroClient getClient() {
		return new WhakaoroClient(url);
	}

}
