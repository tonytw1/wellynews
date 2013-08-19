package nz.co.searchwellington.feeds.reading;

import java.util.List;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;

public interface FeedItemFetcher {

	public List<FeedNewsitem> fetchFeedItems(Feed feed);

}