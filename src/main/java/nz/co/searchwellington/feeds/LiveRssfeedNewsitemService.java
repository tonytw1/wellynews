package nz.co.searchwellington.feeds;

import java.util.List;

import nz.co.searchwellington.feeds.reading.FeedItemFetcher;
import nz.co.searchwellington.feeds.reading.WhakaokoFeedReader;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.caching.CachableService;

@Component
public class LiveRssfeedNewsitemService implements CachableService<Feed, List<FrontendFeedNewsitem>> {
		
    private static final int FIVE_MINUTES = 60 * 5;
    
	private final FeedItemFetcher httpFetchFeedReader;
	
    @Autowired
    public LiveRssfeedNewsitemService(WhakaokoFeedReader httpFetchFeedReader) {
		this.httpFetchFeedReader = httpFetchFeedReader;
	}

    @Override
	public List<FrontendFeedNewsitem> callService(Feed feed) {
		return getFeedNewsitems(feed);
	}
    
	@Override
	public String getCacheKeyFor(Feed feed) {
		return "feednewsitems" + DigestUtils.md5Hex(feed.getUrl());
	}

	@Override
	public int getTTL() {
		return FIVE_MINUTES;
	}
    
	public List<FrontendFeedNewsitem> getFeedNewsitems(Feed feed) {
		return httpFetchFeedReader.fetchFeedItems(feed);
    }
	
}
