package nz.co.searchwellington.feeds.reading;

import org.springframework.stereotype.Component;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.frontend.FrontendFeed;
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem;
import nz.co.searchwellington.model.frontend.FrontendImage;
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem;

@Component
public class WhakaokoFeedItemMapper {

	public FrontendFeedNewsitem mapWhakaokoFeeditem(Feed feed, FeedItem feedItem) {
		FrontendFeedNewsitem frontendFeedNewsitem = new FrontendFeedNewsitem();		
		if (feed != null) {
			FrontendFeed frontendFeed = new FrontendFeed();
			frontendFeed.setUrlWords(feed.getUrlWords());				
			frontendFeedNewsitem.setFeed(frontendFeed);
			
			if (feed.getPublisher() != null) {
				frontendFeedNewsitem.setPublisherName(feed.getPublisherName());
			}
		}
		
		frontendFeedNewsitem.setName(feedItem.getTitle());
		frontendFeedNewsitem.setUrl(feedItem.getUrl());
		frontendFeedNewsitem.setDate(feedItem.getDate());
		frontendFeedNewsitem.setDescription(feedItem.getBody());
		
		if (feedItem.getImageUrl() != null) {
			frontendFeedNewsitem.setImage(new FrontendImage(feedItem.getImageUrl()));
		}
		return frontendFeedNewsitem;
	}
	
}
