package nz.co.searchwellington.feeds.reading;

import java.util.List;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem;

import org.apache.log4j.Logger;
import org.elasticsearch.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.http.HttpBadRequestException;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.eelpieconsulting.common.http.HttpForbiddenException;
import uk.co.eelpieconsulting.common.http.HttpNotFoundException;
import uk.co.eelpieconsulting.whakaoro.client.WhakaoroClient;
import uk.co.eelpieconsulting.whakaoro.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem;

@Component
public class WhakaokoFeedReader implements FeedItemFetcher {
	
	private final Logger log = Logger.getLogger(WhakaokoFeedReader.class);
	
	private final WhakaoroClient client;
	private final WhakaokoFeedItemMapper whakaokoFeedItemMapper;
	
	@Autowired
	public WhakaokoFeedReader(WhakaoroClientFactory whakaoroClientFactory, WhakaokoFeedItemMapper whakaokoFeedItemMapper) {
		this.whakaokoFeedItemMapper = whakaokoFeedItemMapper;
		this.client = whakaoroClientFactory.getClient();
	}
	
	@Override
	public List<FrontendFeedNewsitem> fetchFeedItems(Feed feed) {
		log.info("Fetching feed items for feed with whakaoko id: " + feed.getWhakaokoId());
		if (feed.getWhakaokoId() == null) {
			log.warn("Feed has no whakaoro id; skipping: " + feed.getName());
		}
		
		try {
			final List<FrontendFeedNewsitem> results = Lists.newArrayList();
			for (FeedItem feedItem : client.getSubscriptionFeedItems(feed.getWhakaokoId())) {				
				FrontendFeedNewsitem frontendFeedNewsitem = whakaokoFeedItemMapper.mapWhakaokoFeeditem(feed, feedItem);				
				results.add(frontendFeedNewsitem);
			}
			log.info("Got " + results.size() + " items from whakaoko");
			return results;
			
		} catch (HttpNotFoundException e) {
			log.error(e);
		} catch (HttpBadRequestException e) {
			log.error(e);
		} catch (HttpForbiddenException e) {
			log.error(e);
		} catch (ParsingException e) {
			log.error(e);
		} catch (HttpFetchException e) {
			log.error(e);
		}
		
		return Lists.newArrayList();		
	}
	
}
