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
        return new FrontendFeedNewsitem(feedItem.getTitle(),
            feedItem.getUrl(), feedItem.getDate(), feedItem.getBody(),
            feedItem.getPlace(), makeFrontendFeed(feed),
                feed.getPublisher() != null ? feed.getPublisher().getName() : null,
                feedItem.getImageUrl() != null ? new FrontendImage(feedItem.getImageUrl()): null);
	}

    private FrontendFeed makeFrontendFeed(Feed feed) {
        if (feed == null) {
            return null;
        }
        FrontendFeed frontendFeed = new FrontendFeed();
        frontendFeed.setUrlWords(feed.getUrlWords());
        return frontendFeed;
    }

}
