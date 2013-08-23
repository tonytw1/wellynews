package nz.co.searchwellington.feeds.reading;

import java.util.List;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FrontendFeedNewsitem;

public interface FeedItemFetcher {

	public List<FrontendFeedNewsitem> fetchFeedItems(Feed feed);

}