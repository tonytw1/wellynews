package nz.co.searchwellington.feeds;

import java.util.Calendar;
import java.util.List;

import nz.co.searchwellington.dates.DateFormatter;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SuggestionRepository;
import nz.co.searchwellington.tagging.AutoTaggingService;
import nz.co.searchwellington.utils.UrlCleaner;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class FeedReader {
      
	private static Logger log = Logger.getLogger(FeedReader.class);
    
    private ResourceRepository resourceDAO;
    private RssfeedNewsitemService rssfeedNewsitemService;
    private FeedAcceptanceDecider feedAcceptanceDecider;
    private DateFormatter dateFormatter;   
    private UrlCleaner urlCleaner;
    private SuggestionRepository suggestionDAO;
    private ContentUpdateService contentUpdateService;
	private FeedItemAcceptor feedItemAcceptor;
    private AutoTaggingService autoTagger;
    
    public FeedReader() {        
    }
    
	public FeedReader(ResourceRepository resourceDAO,
			RssfeedNewsitemService rssfeedNewsitemService,
			FeedAcceptanceDecider feedAcceptanceDecider,
			DateFormatter dateFormatter, UrlCleaner urlCleaner,
			SuggestionRepository suggestionDAO,
			ContentUpdateService contentUpdateService,
			FeedItemAcceptor feedItemAcceptor,
			AutoTaggingService autoTagger) {
		this.resourceDAO = resourceDAO;
		this.rssfeedNewsitemService = rssfeedNewsitemService;
		this.feedAcceptanceDecider = feedAcceptanceDecider;
		this.dateFormatter = dateFormatter;
		this.urlCleaner = urlCleaner;
		this.suggestionDAO = suggestionDAO;
		this.contentUpdateService = contentUpdateService;
		this.feedItemAcceptor = feedItemAcceptor;
		this.autoTagger= autoTagger;
	}
		
	@Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processFeed(int feedId) {
    	Feed feed = (Feed) resourceDAO.loadResourceById(feedId);    	
    	log.info("Processing feed: " + feed.getName() + ". Last read: " + dateFormatter.formatDate(feed.getLastRead(), DateFormatter.TIME_DAY_MONTH_YEAR_FORMAT));               

    	// TODO can this move onto the enum?
    	final boolean shouldLookAtFeed =  feed.getAcceptancePolicy() != null && feed.getAcceptancePolicy().equals("accept") 
        	|| feed.getAcceptancePolicy().equals("accept_without_dates")
        	|| feed.getAcceptancePolicy().equals("suggest");

        if (shouldLookAtFeed) {
          processFeedItems(feed);
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
	
	private void processFeedItems(Feed feed) {
		List<FeedNewsitem> feedNewsitems = rssfeedNewsitemService.getFeedNewsitems(feed);
		if (!feedNewsitems.isEmpty()) {
			feed.setHttpStatus(200);			
			for (FeedNewsitem feednewsitem : feedNewsitems) {
				String cleanSubmittedItemUrl = urlCleaner.cleanSubmittedItemUrl(feednewsitem.getUrl());
				feednewsitem.setUrl(cleanSubmittedItemUrl);
		    
				if (feed.getAcceptancePolicy().startsWith("accept")) {
					boolean acceptThisItem = feedAcceptanceDecider.getAcceptanceErrors(feednewsitem, feed.getAcceptancePolicy()).size() == 0;
					if (acceptThisItem) {
						User feedReaderUser = null;	// TODO
						Newsitem newsitem = feedItemAcceptor.acceptFeedItem(feedReaderUser, feednewsitem);										   
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
