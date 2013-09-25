package nz.co.searchwellington.feeds;

import java.util.Calendar;
import java.util.List;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedAcceptancePolicy;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.queues.LinkCheckerQueue;
import nz.co.searchwellington.repositories.HibernateResourceDAO;
import nz.co.searchwellington.tagging.AutoTaggingService;
import nz.co.searchwellington.utils.UrlCleaner;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import uk.co.eelpieconsulting.common.dates.DateFormatter;

@Component
public class FeedReader {
      
	private static Logger log = Logger.getLogger(FeedReader.class);
	
    private HibernateResourceDAO resourceDAO;
    private RssfeedNewsitemService rssfeedNewsitemService;
    private FeedAcceptanceDecider feedAcceptanceDecider;
    private DateFormatter dateFormatter;   
    private UrlCleaner urlCleaner;
	private FeedReaderUpdateService feedReaderUpdateService;
	private ContentUpdateService contentUpdateService;
	private LinkCheckerQueue linkCheckerQueue;
    
    public FeedReader() {        
    }
    
    @Autowired
	public FeedReader(HibernateResourceDAO resourceDAO,
			RssfeedNewsitemService rssfeedNewsitemService,
			FeedAcceptanceDecider feedAcceptanceDecider,
			UrlCleaner urlCleaner,
			ContentUpdateService contentUpdateService,
			FeedItemAcceptor feedItemAcceptor,
			AutoTaggingService autoTagger,
			FeednewsItemToNewsitemService feednewsItemToNewsitemService,
			LinkCheckerQueue linkCheckerQueue, FeedReaderUpdateService feedReaderUpdateService) {
		this.resourceDAO = resourceDAO;
		this.rssfeedNewsitemService = rssfeedNewsitemService;
		this.feedAcceptanceDecider = feedAcceptanceDecider;
		this.urlCleaner = urlCleaner;
		this.feedAcceptanceDecider = feedAcceptanceDecider;
		this.feedReaderUpdateService = feedReaderUpdateService;
		this.contentUpdateService = contentUpdateService;
		this.linkCheckerQueue = linkCheckerQueue;
		dateFormatter = new DateFormatter();
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void processFeed(int feedId, User loggedInUser, FeedAcceptancePolicy manuallySpecifiedAcceptancePolicy) {	// TODO interface should be feeds not feed ids?
		Feed feed = (Feed) resourceDAO.loadResourceById(feedId); 
		processFeed(feed, loggedInUser, manuallySpecifiedAcceptancePolicy);		
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void processFeed(int feedId, User loggedInUser) {
		Feed feed = (Feed) resourceDAO.loadResourceById(feedId);
		processFeed(feed, loggedInUser, feed.getAcceptancePolicy());		
	}
	
    private void processFeed(Feed feed, User feedReaderUser, FeedAcceptancePolicy acceptancePolicy) {
    	try {
			log.info("Processing feed: " + feed.getName() + " using acceptance policy '" + acceptancePolicy + "'. Last read: " + dateFormatter.timeSince(feed.getLastRead()));
			final List<FrontendFeedNewsitem> feedNewsitems = rssfeedNewsitemService.getFeedNewsitems(feed);
			log.info("Feed contains " + feedNewsitems.size() + " items");			
			feed.setHttpStatus(!feedNewsitems.isEmpty() ? 200 : -3);
			
			if (acceptancePolicy.shouldReadFeed()) {
				processFeedItems(feed, feedReaderUser, acceptancePolicy, feedNewsitems);	          
			}
			
	        markFeedAsRead(feed);	        
	        log.info("Done processing feed.");
	        
    	} catch (Exception e) {
    		log.error(e, e);
    	}
    	return;
    }
	
	private void processFeedItems(Feed feed, User feedReaderUser, FeedAcceptancePolicy acceptancePolicy, List<FrontendFeedNewsitem> feedNewsitems) {		
		log.info ("Accepting feed items");
		for (FrontendFeedNewsitem feednewsitem : feedNewsitems) {
				// TODO new up a new copy before modifying
				final String cleanSubmittedItemUrl = urlCleaner.cleanSubmittedItemUrl(feednewsitem.getUrl());
				feednewsitem.setUrl(cleanSubmittedItemUrl);				
					
				final List<String> acceptanceErrors = feedAcceptanceDecider.getAcceptanceErrors(feed, feednewsitem, acceptancePolicy);
				final boolean acceptThisItem = acceptanceErrors.isEmpty();
				if (acceptThisItem) {
					log.info("Accepting newsitem: " + feednewsitem.getUrl());
					linkCheckerQueue.add(feedReaderUpdateService.acceptNewsitem(feed, feedReaderUser, feednewsitem));
				} else {
					log.info("Not accepting " + feednewsitem.getUrl() + " due to acceptance errors: " + acceptanceErrors);
				}
			}		
	}
	
	public void markFeedAsRead(Feed feed) {
		feed.setLatestItemDate(rssfeedNewsitemService.getLatestPublicationDate(feed));
		log.info("Feed latest item publication date is: " + feed.getLatestItemDate());
		feed.setLastRead(Calendar.getInstance().getTime());
		contentUpdateService.update(feed);
	}
	
}
