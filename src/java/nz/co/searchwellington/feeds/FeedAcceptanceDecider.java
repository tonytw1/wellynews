package nz.co.searchwellington.feeds;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SuggestionRepository;
import nz.co.searchwellington.repositories.SupressionRepository;
import nz.co.searchwellington.utils.UrlCleaner;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


public class FeedAcceptanceDecider {
    
    static Logger log = Logger.getLogger(FeedAcceptanceDecider.class);
    
    private ResourceRepository resourceDAO;
    private SupressionRepository supressionDAO;
    private UrlCleaner urlCleaner;
	private SuggestionRepository suggestionDAO;
 
	
    public FeedAcceptanceDecider() {
	}


	public FeedAcceptanceDecider(ResourceRepository resourceDAO, SupressionRepository supressionDAO, UrlCleaner urlCleaner, SuggestionRepository suggestionDAO) {
        this.resourceDAO = resourceDAO;
        this.supressionDAO = supressionDAO;
        this.urlCleaner = urlCleaner;
        this.suggestionDAO = suggestionDAO;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW) 
    public List<String> getAcceptanceErrors(FeedNewsitem resource, String feedAcceptancePolicy) {
        List<String> acceptanceErrors = new ArrayList<String>();
        
        final String cleanedUrl = urlCleaner.cleanSubmittedItemUrl(resource.getUrl());
		final boolean isSuppressed = supressionDAO.isSupressed(cleanedUrl);
		log.info("Is feed item url '" + cleanedUrl + "' supressed: " + isSuppressed);
        if (isSuppressed) {
            acceptanceErrors.add("This item is supressed");
        }    
            
        final boolean titleIsBlank = resource.getName() != null && resource.getName().equals("");
        if (titleIsBlank) {
            acceptanceErrors.add("Item has no title");
        }
           
        lessThanOneWeekOld(resource, feedAcceptancePolicy, acceptanceErrors);
        hasDateInTheFuture(resource, acceptanceErrors);                
        alreadyHaveThisFeedItem(resource, acceptanceErrors);
        alreadyHaveAnItemWithTheSameHeadlineFromTheSamePublisherWithinTheLastMonth(resource, acceptanceErrors);
        
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
    
    
    private void hasDateInTheFuture(Resource resource, List<String> acceptanceErrors) {
    	Calendar oneDayFromNow = Calendar.getInstance();
    	oneDayFromNow.add(Calendar.DATE, 1);  	
        if(resource.getDate() != null && resource.getDate().after(oneDayFromNow.getTime())) {
            StringWriter message = new StringWriter();
            message.append("Has date in the future");            
            message.append(" (" + resource.getDate().toString() + " is after " + oneDayFromNow.getTime().toString() + ")");
        	acceptanceErrors.add(message.toString());        
        }    
	}


	private void alreadyHaveThisFeedItem(Resource resourceFromFeed, List<String> acceptanceErrors) {
        String url = urlCleaner.cleanSubmittedItemUrl(resourceFromFeed.getUrl());
        if (resourceDAO.loadResourceByUrl(url) !=  null) {
            log.debug("A resource with url '" + resourceFromFeed.getUrl() + "' already exists; not accepting.");
            acceptanceErrors.add("Item already exists");
        }
    }
    
	
	private void alreadyHaveAnItemWithTheSameHeadlineFromTheSamePublisherWithinTheLastMonth(FeedNewsitem resource, List<String> acceptanceErrors) {
		 if (resourceDAO.loadNewsitemByHeadlineAndPublisherWithinLastMonth(resource.getName(), resource.getPublisher()) !=  null) {
			 log.info("A recent resource from the same publisher with the same headline '" + resource.getName() + "' already exists; not accepting.");
	          acceptanceErrors.add("A recent resource from the same publisher with the same headline already exists; not accepting.");
		 }
	}
    
	
    public void lessThanOneWeekOld(Resource newsitem, String feedAcceptancePolicy, List<String> acceptanceErrors) {      
        if (feedAcceptancePolicy != null && feedAcceptancePolicy.equals("accept_without_dates")) {
            return;                        
        }
        Calendar oneWeekAgo = Calendar.getInstance();
        oneWeekAgo.add(Calendar.DATE, -7);
        if (!(newsitem.getDate() != null && newsitem.getDate().after(oneWeekAgo.getTime()))) {
            acceptanceErrors.add("Item is more than one week old");            
        }
    }
    
}
