package nz.co.searchwellington.feeds;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Suggestion;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Twit;
import nz.co.searchwellington.model.Website;

public abstract class RssfeedNewsitemService {

	public abstract List<FeedNewsitem> getFeedNewsitems(Feed feed);

	public final Date getLatestPublicationDate(Feed feed) {
	        Date latestPublicationDate = null;
	        List<FeedNewsitem> feeditems = getFeedNewsitems(feed);
	        for (Resource resource : feeditems) {
	            if (resource.getDate() != null && (latestPublicationDate == null || resource.getDate().after(latestPublicationDate))) {
	                latestPublicationDate = resource.getDate();           
	            }
	       }
	        return latestPublicationDate;
	}
	
	public final Newsitem makeNewsitemFromFeedItem(FeedNewsitem feedNewsitem, Website feedPublisher) {
	// TODO constructor calls should be in the resourceDAO?
	    	Newsitem newsitem = new NewsitemImpl(0, feedNewsitem.getName(), feedNewsitem.getUrl(), feedNewsitem.getDescription(), feedNewsitem.getDate(), feedPublisher, 
	    			new HashSet<Tag>(),
	    			new HashSet<DiscoveredFeed>(), new HashSet<Twit>());
	    	newsitem.setImage(feedNewsitem.getImage());
	    	return newsitem;
	}
	
	
	public FeedNewsitem getFeedNewsitemByUrl(Suggestion suggestion) {
		List<FeedNewsitem> feedNewsitems = this.getFeedNewsitems(suggestion.getFeed());
		Iterator<FeedNewsitem> i = feedNewsitems.iterator();
		while (i.hasNext()) {
			FeedNewsitem feedNewsitem = i.next();
			if (feedNewsitem.getUrl().equals(suggestion.getUrl())) {
				return feedNewsitem;
			}
		}
		return null;
	}
	
}