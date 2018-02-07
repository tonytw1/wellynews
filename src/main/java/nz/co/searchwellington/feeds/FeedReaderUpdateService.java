package nz.co.searchwellington.feeds;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.tagging.AutoTaggingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FeedReaderUpdateService {
	
    private ContentUpdateService contentUpdateService;
	private FeedItemAcceptor feedItemAcceptor;
    private AutoTaggingService autoTagger;
    private FeednewsItemToNewsitemService feednewsItemToNewsitemService;

    public FeedReaderUpdateService() {
	}
    
	@Autowired
	public FeedReaderUpdateService(ContentUpdateService contentUpdateService,
			FeedItemAcceptor feedItemAcceptor, AutoTaggingService autoTagger,
			FeednewsItemToNewsitemService feednewsItemToNewsitemService) {
		this.contentUpdateService = contentUpdateService;
		this.feedItemAcceptor = feedItemAcceptor;
		this.autoTagger = autoTagger;
		this.feednewsItemToNewsitemService = feednewsItemToNewsitemService;
	}

	public int acceptNewsitem(Feed feed, User feedReaderUser, FrontendFeedNewsitem feednewsitem) {
		final Newsitem newsitem = feednewsItemToNewsitemService.makeNewsitemFromFeedItem(feed, feednewsitem);
		feedItemAcceptor.acceptFeedItem(feedReaderUser, newsitem);

		contentUpdateService.create(newsitem);
		autoTagger.autotag(newsitem);
		contentUpdateService.update(newsitem);
		return newsitem.getId();
	}

}
