package nz.co.searchwellington.feeds;

import java.util.Calendar;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.queues.LinkCheckerQueue;
import nz.co.searchwellington.tagging.AutoTaggingService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FeedReaderUpdateService {
	
	private static Logger log = Logger.getLogger(FeedReader.class);

    private ContentUpdateService contentUpdateService;
	private FeedItemAcceptor feedItemAcceptor;
    private AutoTaggingService autoTagger;
    private FeednewsItemToNewsitemService feednewsItemToNewsitemService;
	private LinkCheckerQueue linkCheckerQueue;
    private RssfeedNewsitemService rssfeedNewsitemService;

    public FeedReaderUpdateService() {
	}
    
	@Autowired
	public FeedReaderUpdateService(ContentUpdateService contentUpdateService,
			FeedItemAcceptor feedItemAcceptor, AutoTaggingService autoTagger,
			FeednewsItemToNewsitemService feednewsItemToNewsitemService,
			LinkCheckerQueue linkCheckerQueue, RssfeedNewsitemService rssfeedNewsitemService) {
		this.contentUpdateService = contentUpdateService;
		this.feedItemAcceptor = feedItemAcceptor;
		this.autoTagger = autoTagger;
		this.feednewsItemToNewsitemService = feednewsItemToNewsitemService;
		this.linkCheckerQueue = linkCheckerQueue;
		this.rssfeedNewsitemService = rssfeedNewsitemService;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int acceptNewsitem(Feed feed, User feedReaderUser, FrontendFeedNewsitem feednewsitem) {
		final Newsitem newsitem = feednewsItemToNewsitemService.makeNewsitemFromFeedItem(feed, feednewsitem);
		feedItemAcceptor.acceptFeedItem(feedReaderUser, newsitem);

		contentUpdateService.create(newsitem);
		autoTagger.autotag(newsitem);
		contentUpdateService.update(newsitem);
		return newsitem.getId();
	}

}
