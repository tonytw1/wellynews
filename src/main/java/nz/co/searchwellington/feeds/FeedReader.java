package nz.co.searchwellington.feeds;

import java.util.Calendar;
import java.util.List;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedAcceptancePolicy;
import nz.co.searchwellington.model.FrontendFeedNewsitem;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.User;
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
	public void processFeed(int feedId, User loggedInUser, FeedAcceptancePolicy manuallySpecifiedAcceptancePolicy) {
		Feed feed = (Feed) resourceDAO.loadResourceById(feedId); 
		processFeed(feed, loggedInUser, manuallySpecifiedAcceptancePolicy.getName());		
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processFeed(int feedId, User loggedInUser) {
		Feed feed = (Feed) resourceDAO.loadResourceById(feedId);
		processFeed(feed, loggedInUser, feed.getAcceptancePolicy());		
	}
	
    private void processFeed(Feed feed, User feedReaderUser, String acceptancePolicy) {		    	
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
        return;
    }
	
	private void processFeedItems(Feed feed, User feedReaderUser, String acceptancePolicy) {		
		List<FrontendFeedNewsitem> feedNewsitems = rssfeedNewsitemService.getFeedNewsitems(feed);
		if (!feedNewsitems.isEmpty()) {
			feed.setHttpStatus(200);			
			for (FrontendFeedNewsitem feednewsitem : feedNewsitems) {
				// TODO new up a new copy before modifying
				String cleanSubmittedItemUrl = urlCleaner.cleanSubmittedItemUrl(feednewsitem.getUrl());
				feednewsitem.setUrl(cleanSubmittedItemUrl);
		    
				if (acceptancePolicy.startsWith("accept")) {
					boolean acceptThisItem = feedAcceptanceDecider.getAcceptanceErrors(feed, feednewsitem, acceptancePolicy).isEmpty();
					if (acceptThisItem) {
						Newsitem newsitem = feednewsItemToNewsitemService.makeNewsitemFromFeedItem(feed, feednewsitem);
						feedItemAcceptor.acceptFeedItem(feedReaderUser, newsitem);
						
						log.info("Geocode of accepted newsitem after accept feed item: " + newsitem.getGeocode());
						
						contentUpdateService.create(newsitem);
						autoTagger.autotag(newsitem);
						contentUpdateService.update(newsitem);
					}
					
				} else {                	
					if (feedAcceptanceDecider.shouldSuggest(feednewsitem)) {
						log.info("Suggesting: " + feed.getName() + ": " + feednewsitem.getName());
						suggestionDAO.addSuggestion(suggestionDAO.createSuggestion(feed, feednewsitem.getUrl(), new DateTime().toDate()));
					}
				}
			}
			
		 } else {
         	log.warn("Incoming feed '" + feed.getName() + "' contained no items");
         	feed.setHttpStatus(-3);
         }
	}
	
}
