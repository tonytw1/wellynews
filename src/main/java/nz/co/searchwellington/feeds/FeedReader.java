package nz.co.searchwellington.feeds;

import java.util.Calendar;
import java.util.List;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedAcceptancePolicy;
import nz.co.searchwellington.model.Newsitem;
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
    private ContentUpdateService contentUpdateService;
	private FeedItemAcceptor feedItemAcceptor;
    private AutoTaggingService autoTagger;
    private FeednewsItemToNewsitemService feednewsItemToNewsitemService;
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
			LinkCheckerQueue linkCheckerQueue) {
		this.resourceDAO = resourceDAO;
		this.rssfeedNewsitemService = rssfeedNewsitemService;
		this.feedAcceptanceDecider = feedAcceptanceDecider;
		this.urlCleaner = urlCleaner;
		this.contentUpdateService = contentUpdateService;
		this.feedItemAcceptor = feedItemAcceptor;
		this.autoTagger= autoTagger;
		this.feedAcceptanceDecider = feedAcceptanceDecider;
		this.feednewsItemToNewsitemService = feednewsItemToNewsitemService;
		this.linkCheckerQueue = linkCheckerQueue;
		dateFormatter = new DateFormatter();
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processFeed(int feedId, User loggedInUser, FeedAcceptancePolicy manuallySpecifiedAcceptancePolicy) {	// TODO interface should be feeds not feed ids?
		Feed feed = (Feed) resourceDAO.loadResourceById(feedId); 
		processFeed(feed, loggedInUser, manuallySpecifiedAcceptancePolicy);		
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processFeed(int feedId, User loggedInUser) {
		Feed feed = (Feed) resourceDAO.loadResourceById(feedId);
		processFeed(feed, loggedInUser, feed.getAcceptancePolicy());		
	}
	
    private void processFeed(Feed feed, User feedReaderUser, FeedAcceptancePolicy acceptancePolicy) {
    	try {
			log.info("Processing feed: " + feed.getName()
					+ " using acceptance policy '" + acceptancePolicy
					+ "'. Last read: " + dateFormatter.timeSince(feed.getLastRead()));
	
	    	// TODO can this move onto the enum?
			final boolean shouldLookAtFeed =  acceptancePolicy == FeedAcceptancePolicy.ACCEPT || acceptancePolicy == FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES || acceptancePolicy == FeedAcceptancePolicy.SUGGEST; 	        
	        if (shouldLookAtFeed) {
	          processFeedItems(feed, feedReaderUser, acceptancePolicy);
	          
	        } else {
	        	log.debug("Ignoring feed " + feed.getName() + "; acceptance policy is not set to accept or suggest");
	        }
	        
	        feed.setLatestItemDate(rssfeedNewsitemService.getLatestPublicationDate(feed));
	        log.info("Feed latest item publication date is: " + feed.getLatestItemDate());                    
	        feed.setLastRead(Calendar.getInstance().getTime());
	        contentUpdateService.update(feed);
	
	        log.info("Done processing feed.");

    	} catch (Exception e) {
    		log.error(e, e);
    	}
    	return;
    }
	
	private void processFeedItems(Feed feed, User feedReaderUser, FeedAcceptancePolicy acceptancePolicy) {		
		final List<FrontendFeedNewsitem> feedNewsitems = rssfeedNewsitemService.getFeedNewsitems(feed);
		if (!feedNewsitems.isEmpty()) {
			log.info("Feed contains " + feedNewsitems.size() + " items");
			feed.setHttpStatus(200);
			
			if (acceptancePolicy == FeedAcceptancePolicy.ACCEPT || acceptancePolicy == FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES) { // TODO move to enum method
				log.info ("Accepting feed items");

				for (FrontendFeedNewsitem feednewsitem : feedNewsitems) {
					// TODO new up a new copy before modifying
					String cleanSubmittedItemUrl = urlCleaner.cleanSubmittedItemUrl(feednewsitem.getUrl());
					feednewsitem.setUrl(cleanSubmittedItemUrl);				
					
					final List<String> acceptanceErrors = feedAcceptanceDecider.getAcceptanceErrors(feed, feednewsitem, acceptancePolicy);
					final boolean acceptThisItem = acceptanceErrors.isEmpty();
					if (acceptThisItem) {
						log.info("Accepting newsitem: " + feednewsitem.getUrl());
						acceptNewsitem(feed, feedReaderUser, feednewsitem);
						
					} else {
						log.info("Not accepting " + feednewsitem.getUrl() + " due to acceptance errors: " + acceptanceErrors);
					}
				}
				
			} else {
				log.info("Feed acceptance is: " + acceptancePolicy + "; ignoring");
			}
			
		 } else {
         	log.warn("Incoming feed '" + feed.getName() + "' contained no items");
         	feed.setHttpStatus(-3);
         }
	}
	
	private void acceptNewsitem(Feed feed, User feedReaderUser, FrontendFeedNewsitem feednewsitem) {
		final Newsitem newsitem = feednewsItemToNewsitemService.makeNewsitemFromFeedItem(feed, feednewsitem);
		feedItemAcceptor.acceptFeedItem(feedReaderUser, newsitem);
								
		contentUpdateService.create(newsitem);
		autoTagger.autotag(newsitem);
		contentUpdateService.update(newsitem);
		
		linkCheckerQueue.add(newsitem.getId());
	}
	
}
