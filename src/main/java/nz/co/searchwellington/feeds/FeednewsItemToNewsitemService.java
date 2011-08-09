package nz.co.searchwellington.feeds;

import java.util.HashSet;

import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Twit;

public class FeednewsItemToNewsitemService {
	
	// TODO merge with addSuppressAndLocalCopyInformation?
	public Newsitem makeNewsitemFromFeedItem(Feed feed, FeedNewsitem feedNewsitem) {
		// TODO why are we newing up an instance of our superclass?
	    String description =  feedNewsitem.getDescription() != null ? feedNewsitem.getDescription() : "";
		Newsitem newsitem = new NewsitemImpl(0, feedNewsitem.getName(), feedNewsitem.getUrl(), description, feedNewsitem.getDate(), feed.getPublisher(), new HashSet<DiscoveredFeed>(), null, new HashSet<Twit>());
	    newsitem.setImage(feedNewsitem.getImage());
	    newsitem.setFeed(feed);
	    newsitem.setPublisher(feed.getPublisher());
	    newsitem.setGeocode(feedNewsitem.getGeocode());
	    return newsitem;
	}
	
}
