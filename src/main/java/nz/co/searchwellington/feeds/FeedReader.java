package nz.co.searchwellington.feeds;

import java.util.Calendar;
import java.util.List;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedAcceptancePolicy;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.repositories.HibernateResourceDAO;
import nz.co.searchwellington.repositories.SuggestionDAO;
import nz.co.searchwellington.tagging.AutoTaggingService;
import nz.co.searchwellington.utils.UrlCleaner;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
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
    private SuggestionDAO suggestionDAO;
    private ContentUpdateService contentUpdateService;
	private FeedItemAcceptor feedItemAcceptor;
    private AutoTaggingService autoTagger;
    private FeednewsItemToNewsitemService feednewsItemToNewsitemService;
    
    public FeedReader() {        
    }
    
    @Autowired
	public FeedReader(HibernateResourceDAO resourceDAO,
			RssfeedNewsitemService rssfeedNewsitemService,
			FeedAcceptanceDecider feedAcceptanceDecider,
			UrlCleaner urlCleaner,
			SuggestionDAO suggestionDAO,
			ContentUpdateService contentUpdateService,
			FeedItemAcceptor feedItemAcceptor,
			AutoTaggingService autoTagger,
			FeednewsItemToNewsitemService feednewsItemToNewsitemService) {
		this.resourceDAO = resourceDAO;
		this.rssfeedNewsitemService = rssfeedNewsitemService;
		this.feedAcceptanceDecider = feedAcceptanceDecider;
		this.urlCleaner = urlCleaner;
		this.suggestionDAO = suggestionDAO;
		this.contentUpdateService = contentUpdateService;
		this.feedItemAcceptor = feedItemAcceptor;
		this.autoTagger= autoTagger;
		this.feedAcceptanceDecider = feedAcceptanceDecider;
		this.feednewsItemToNewsitemService = feednewsItemToNewsitemService;
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
			final boolean shouldLookAtFeed =  acceptancePolicy != null && acceptancePolicy.equals("accept") 
	        	|| acceptancePolicy.equals("accept_without_dates")
	        	|| acceptancePolicy.equals("suggest");
	
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
    		log.error(e);
    	}
    	return;
    }
	
	private void processFeedItems(Feed feed, User feedReaderUser, FeedAcceptancePolicy acceptancePolicy) {		
		final List<FrontendFeedNewsitem> feedNewsitems = rssfeedNewsitemService.getFeedNewsitems(feed);
		if (!feedNewsitems.isEmpty()) {
			feed.setHttpStatus(200);			
			for (FrontendFeedNewsitem feednewsitem : feedNewsitems) {
				// TODO new up a new copy before modifying
				String cleanSubmittedItemUrl = urlCleaner.cleanSubmittedItemUrl(feednewsitem.getUrl());
				feednewsitem.setUrl(cleanSubmittedItemUrl);
				
				if (acceptancePolicy == FeedAcceptancePolicy.ACCEPT || acceptancePolicy == FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES) { // TODO move to enum method
					final boolean acceptThisItem = feedAcceptanceDecider.getAcceptanceErrors(feed, feednewsitem, acceptancePolicy).isEmpty();
					if (acceptThisItem) {
						log.info("Accepting newsitem: " + feednewsitem.getUrl());
						acceptNewsitem(feed, feedReaderUser, feednewsitem);
					}
					
				} else {
					if (feedAcceptanceDecider.shouldSuggest(feednewsitem)) {
						suggestNewsitem(feed, feednewsitem);	// TODO suggestions should be worked out on the fly
					}
				}
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
	}
	
	private void suggestNewsitem(Feed feed, FrontendFeedNewsitem feednewsitem) {
		log.info("Suggesting: " + feed.getName() + ": " + feednewsitem.getName());
		suggestionDAO.addSuggestion(suggestionDAO.createSuggestion(feed, feednewsitem.getUrl(), new DateTime().toDate()));
	}
	
}
