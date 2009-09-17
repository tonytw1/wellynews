package nz.co.searchwellington.feeds;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import nz.co.searchwellington.dates.DateFormatter;
import nz.co.searchwellington.mail.Notifier;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.LinkCheckerQueue;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SuggestionDAO;
import nz.co.searchwellington.tagging.AutoTaggingService;
import nz.co.searchwellington.utils.UrlCleaner;
import nz.co.searchwellington.utils.UrlFilters;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.transaction.annotation.Transactional;

import com.sun.syndication.io.FeedException;


public class FeedReader {
    
    private static final int NUMBER_OF_FEEDS_TO_READ = 256;

	Logger log = Logger.getLogger(FeedReader.class);
    
    private ResourceRepository resourceDAO;
    private RssfeedNewsitemService rssfeedNewsitemService;
    private LinkCheckerQueue linkCheckerQueue;
    private AutoTaggingService autoTagger;   
    private Notifier notifier;
    private String notificationReciept;
    private FeedAcceptanceDecider feedAcceptanceDecider;
    private DateFormatter dateFormatter;   
    private UrlCleaner urlCleaner;
    private SuggestionDAO suggestionDAO;
 
    
    public FeedReader() {        
    }
    
    
    
    public FeedReader(ResourceRepository resourceDAO, RssfeedNewsitemService rssfeedNewsitemService, LinkCheckerQueue linkCheckerQueue, AutoTaggingService autoTagger, Notifier notifier, String notificationReciept, FeedAcceptanceDecider feedAcceptanceDecider, DateFormatter dateFormatter, UrlCleaner urlCleaner, SuggestionDAO suggestionDAO) {
        this.resourceDAO = resourceDAO;
        this.rssfeedNewsitemService = rssfeedNewsitemService;
        this.linkCheckerQueue = linkCheckerQueue;
        this.autoTagger = autoTagger;
        this.notifier = notifier;
        this.notificationReciept = notificationReciept;
        this.feedAcceptanceDecider = feedAcceptanceDecider;      
        this.dateFormatter = dateFormatter;
        this.urlCleaner = urlCleaner;
        this.suggestionDAO = suggestionDAO;
    }


    @Transactional
    public void acceptFeeditems() throws FeedException, IOException {              
        log.info("Accepting feeds.");        
        int processed = 0;        
        for (Feed feed: resourceDAO.getFeedsToRead()) {      
            if (processed < NUMBER_OF_FEEDS_TO_READ) {
                processFeed(feed);
                processed ++;
            }
        }
        log.info("Finished reading feeds.");
    }

 
    public void processFeed(Feed feed) throws FeedException, IOException {        
        log.info("Processing feed: " + feed.getName() + ". Last read: " + dateFormatter.formatDate(feed.getLastRead(), DateFormatter.TIME_DAY_MONTH_YEAR_FORMAT));
       
        // TODO can this move onto the enum?
        boolean shouldLookAtFeed =  feed.getAcceptancePolicy() != null && feed.getAcceptancePolicy().equals("accept") 
        	|| feed.getAcceptancePolicy().equals("accept_without_dates")
        	|| feed.getAcceptancePolicy().equals("suggest");

        if (shouldLookAtFeed) {
            List<FeedNewsitem> feedNewsitems = rssfeedNewsitemService.getFeedNewsitems(feed);            
            for (FeedNewsitem feednewsitem : feedNewsitems) {
            	String cleanSubmittedItemUrl = urlCleaner.cleanSubmittedItemUrl(feednewsitem.getUrl());
				feednewsitem.setUrl(cleanSubmittedItemUrl);
                
                if (feed.getAcceptancePolicy().startsWith("accept")) {
                	boolean acceptThisItem = feedAcceptanceDecider.getAcceptanceErrors(feednewsitem, feed.getAcceptancePolicy()).size() == 0;
                	if (acceptThisItem) {                		
                		acceptFeedItem(feednewsitem, feed);               		
                	} 
                	
                } else {                	
                	if (feedAcceptanceDecider.shouldSuggest(feednewsitem)) {
                		log.info("Suggesting: " + feed.getName() + ": " + feednewsitem.getName());
                		suggestionDAO.addSuggestion(suggestionDAO.createSuggestion(feed, feednewsitem.getUrl(), new DateTime().toDate()));
                	}
                }
            }
    
            // TODO what's this all about.
            // Everytime we look at a feed, we should update the latest publication field.
            feed.setLatestItemDate(rssfeedNewsitemService.getLatestPublicationDate(feed));
            
            log.info("Feed latest item publication date is: " + feed.getLatestItemDate());
            
        } else {
            log.debug("Ignoring feed " + feed.getName() + "; acceptance policy is not set to accept");
        }
        
        feed.setLastRead(Calendar.getInstance().getTime());        
        log.info("Done processing feed.");      
    }



    private void acceptFeedItem(FeedNewsitem feeditem, Feed feed) {
        log.info("Accepting: " + feeditem.getName());
        Resource resource = rssfeedNewsitemService.makeNewsitemFromFeedItem(feeditem, feed);     
        log.info("Item body after makeNewsitemFromFeedItem: " + resource.getDescription());
        
        flattenLoudCapsInTitle(resource);
        
        if (resource.getDate() == null) {
        	log.info("Accepting a feeditem with no date; setting date to current time");            
            resource.setDate(new DateTime().toDate());
        }
      
        tagAcceptedFeedItem(resource, feed.getTags());
        log.info("Item body before save: " + resource.getDescription());
        resourceDAO.saveResource(resource);
        linkCheckerQueue.add(resource.getId());
        notifier.sendAcceptanceNotification(notificationReciept, "Accepted newsitem from feed", resource);
    }



	private void flattenLoudCapsInTitle(Resource resource) {
		String flattenedTitle = UrlFilters.lowerCappedSentence(resource.getName());           
        if (!flattenedTitle.equals(resource.getName())) {
        	resource.setName(flattenedTitle);
            log.info("Flatten capitalised sentence to '" + flattenedTitle + "'");
        }
	}



    private void tagAcceptedFeedItem(Resource resource, Set<Tag> feedTags) {       
        for (Tag tag : feedTags) {
            resource.addTag(tag);
        }
        autoTagger.autotag(resource);
    }
    
    
}
