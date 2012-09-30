package nz.co.searchwellington.feeds;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.repositories.HibernateResourceDAO;
import nz.co.searchwellington.repositories.SuggestionDAO;
import nz.co.searchwellington.repositories.SupressionDAO;
import nz.co.searchwellington.utils.UrlCleaner;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FeedAcceptanceDecider {
    
	private static Logger log = Logger.getLogger(FeedAcceptanceDecider.class);
    
    private HibernateResourceDAO resourceDAO;
    private SupressionDAO supressionDAO;
    private UrlCleaner urlCleaner;
	private SuggestionDAO suggestionDAO;
 	
    public FeedAcceptanceDecider() {
	}
    
    @Autowired
	public FeedAcceptanceDecider(HibernateResourceDAO resourceDAO, SupressionDAO supressionDAO, UrlCleaner urlCleaner, SuggestionDAO suggestionDAO) {
        this.resourceDAO = resourceDAO;
        this.supressionDAO = supressionDAO;
        this.urlCleaner = urlCleaner;
        this.suggestionDAO = suggestionDAO;
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW) 
    public List<String> getAcceptanceErrors(Feed feed, FeedNewsitem feedNewsitem, String feedAcceptancePolicy) {
        List<String> acceptanceErrors = new ArrayList<String>();
        
        final String cleanedUrl = urlCleaner.cleanSubmittedItemUrl(feedNewsitem.getUrl());
		final boolean isSuppressed = supressionDAO.isSupressed(cleanedUrl);
		log.debug("Is feed item url '" + cleanedUrl + "' supressed: " + isSuppressed);
        if (isSuppressed) {
            acceptanceErrors.add("This item is supressed");
        }    
            
        final boolean titleIsBlank = feedNewsitem.getName() != null && feedNewsitem.getName().equals("");
        if (titleIsBlank) {
            acceptanceErrors.add("Item has no title");
        }
           
        lessThanOneWeekOld(feedNewsitem, feedAcceptancePolicy, acceptanceErrors);
        hasDateInTheFuture(feedNewsitem, acceptanceErrors);                
        alreadyHaveThisFeedItem(feedNewsitem, acceptanceErrors);
        alreadyHaveAnItemWithTheSameHeadlineFromTheSamePublisherWithinTheLastMonth(feedNewsitem, acceptanceErrors, feed);
        
        return acceptanceErrors;        
    }
    
	public boolean shouldSuggest(FeedNewsitem feednewsitem) {
		String cleanSubmittedItemUrl = urlCleaner.cleanSubmittedItemUrl(feednewsitem.getUrl());
		final boolean isSuppressed = supressionDAO.isSupressed(cleanSubmittedItemUrl);
		final boolean isAlreadySuggested = suggestionDAO.isSuggested(cleanSubmittedItemUrl);
		if (isSuppressed || isAlreadySuggested) {
			return false;
		}
		
		List<String> acceptanceErrors = new ArrayList<String>();		
		alreadyHaveThisFeedItem(feednewsitem, acceptanceErrors);		
		return acceptanceErrors.isEmpty();		
	}
	
    private void hasDateInTheFuture(FeedNewsitem resource, List<String> acceptanceErrors) {
    	Calendar oneDayFromNow = Calendar.getInstance();
    	oneDayFromNow.add(Calendar.DATE, 1);  	
        if(resource.getDate() != null && resource.getDate().after(oneDayFromNow.getTime())) {
            StringWriter message = new StringWriter();
            message.append("Has date in the future");            
            message.append(" (" + resource.getDate().toString() + " is after " + oneDayFromNow.getTime().toString() + ")");
        	acceptanceErrors.add(message.toString());        
        }    
	}
    
	private void alreadyHaveThisFeedItem(FeedNewsitem resourceFromFeed, List<String> acceptanceErrors) {
        String url = urlCleaner.cleanSubmittedItemUrl(resourceFromFeed.getUrl());
        if (resourceDAO.loadResourceByUrl(url) !=  null) {
            log.debug("A resource with url '" + resourceFromFeed.getUrl() + "' already exists; not accepting.");
            acceptanceErrors.add("Item already exists");
        }
    }
    
	// TODO acceptance errors are really moderation votes which human voters should probably be able to overrule.
	private void alreadyHaveAnItemWithTheSameHeadlineFromTheSamePublisherWithinTheLastMonth(FeedNewsitem resource, List<String> acceptanceErrors, Feed feed) {
		 if (resourceDAO.loadNewsitemByHeadlineAndPublisherWithinLastMonth(resource.getName(), feed.getPublisher()) !=  null) {
			 log.info("A recent resource from the same publisher with the same headline '" + resource.getName() + "' already exists; not accepting.");
			 acceptanceErrors.add("A recent resource from the same publisher with the same headline already exists; not accepting.");
		 }
	}
	
    private void lessThanOneWeekOld(FeedNewsitem feedNewsitem, String feedAcceptancePolicy, List<String> acceptanceErrors) {      
        if (feedAcceptancePolicy != null && feedAcceptancePolicy.equals("accept_without_dates")) {
            return;                        
        }
        Calendar oneWeekAgo = Calendar.getInstance();
        oneWeekAgo.add(Calendar.DATE, -7);
        if (!(feedNewsitem.getDate() != null && feedNewsitem.getDate().after(oneWeekAgo.getTime()))) {
            acceptanceErrors.add("Item is more than one week old");            
        }
    }
    
}
