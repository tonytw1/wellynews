package nz.co.searchwellington.feeds;

import java.util.List;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.caching.CachingServiceWrapper;
import uk.co.eelpieconsulting.common.caching.MemcachedCache;

@Component("cachingRssfeedNewsitemService")
public class CachingRssfeedNewsitemService extends CachingServiceWrapper<Feed, List<FrontendFeedNewsitem>> {
	
	@Autowired
	public CachingRssfeedNewsitemService(LiveRssfeedNewsitemService liveRssfeedNewsitemService, MemcachedCache cache) {
		super(liveRssfeedNewsitemService, cache);
	}

	public List<FrontendFeedNewsitem> getFeedNewsitems(Feed feed) {
		return super.callService(feed);
	}
	
}
