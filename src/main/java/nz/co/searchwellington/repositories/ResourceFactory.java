package nz.co.searchwellington.repositories;

import com.google.common.collect.Lists;
import nz.co.searchwellington.model.*;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.HashSet;

@Component
public class ResourceFactory {

	public Newsitem createNewNewsitem() {
		return new NewsitemImpl(0, "", "", "",
				Calendar.getInstance().getTime(), null,
				new HashSet<DiscoveredFeed>());
	}

	public Website createNewWebsite() {
		return new WebsiteImpl(0, "", "", Calendar.getInstance().getTime(), "",
				new HashSet<Feed>(), new HashSet<Watchlist>(),
				new HashSet<DiscoveredFeed>());
	}

	public Feed createNewFeed() {
		return new FeedImpl(0, "", "", "", null, null);
	}

	public Watchlist createNewWatchlist() {
		return new Watchlist(0, "", "", "", null, new HashSet<DiscoveredFeed>());
	}

	public CommentFeed createNewCommentFeed(String commentFeedUrl) {
		return new CommentFeed(0, commentFeedUrl, Lists.<Comment>newArrayList(), null, null);
	}

	public DiscoveredFeed createNewDiscoveredFeed(String discoveredUrl) {
		DiscoveredFeed discoveredFeed = new DiscoveredFeed();
		discoveredFeed.setUrl(discoveredUrl);
		discoveredFeed.setReferences(new HashSet<Resource>());
		return discoveredFeed;
	}

}
