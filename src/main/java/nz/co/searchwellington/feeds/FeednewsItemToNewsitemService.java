package nz.co.searchwellington.feeds;

import java.util.HashSet;

import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.geo.model.Place;

import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Twit;

@Component
public class FeednewsItemToNewsitemService {
	
	// TODO merge with addSuppressAndLocalCopyInformation?
	public Newsitem makeNewsitemFromFeedItem(Feed feed, FeedNewsitem feedNewsitem) {
		// TODO why are we newing up an instance of our superclass?
	    final String description =  feedNewsitem.getDescription() != null ? feedNewsitem.getDescription() : "";
		final Newsitem newsitem = new NewsitemImpl(0, feedNewsitem.getName(), feedNewsitem.getUrl(), description, feedNewsitem.getDate(), feed.getPublisher(), new HashSet<DiscoveredFeed>(), null, new HashSet<Twit>());
	    newsitem.setImage(feedNewsitem.getImage());
	    newsitem.setFeed(feed);
	    newsitem.setPublisher(feed.getPublisher());
	    
	    final Place place = feedNewsitem.getPlace();
	    if (place != null) {
			newsitem.setGeocode(mapPlaceToGeocode(place));
	    }
	    
	    return newsitem;
	}

	private Geocode mapPlaceToGeocode(final Place place) {
		final Geocode geocode = new Geocode(place.getAddress(), 
				place.getLatLong() != null ? place.getLatLong().getLatitude() : null, 
				place.getLatLong() != null ? place.getLatLong().getLongitude() : null,
				place.getOsmId() != null ? place.getOsmId().getId() : null,
				place.getOsmId() != null ? place.getOsmId().getType().toString() : null);
		return geocode;
	}
	
}
