package nz.co.searchwellington.twitter;

import java.util.Calendar;

import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.PublisherGuessingService;
import nz.co.searchwellington.utils.UrlCleaner;

import org.apache.log4j.Logger;

public class TwitterNewsitemBuilderService {

    Logger log = Logger.getLogger(TwitterNewsitemBuilderService.class);
    
    private UrlCleaner urlCleaner;
	private PublisherGuessingService publisherGuessingService;
    
    public TwitterNewsitemBuilderService(UrlCleaner urlCleaner, PublisherGuessingService publisherGuessingService) {     
        this.urlCleaner = urlCleaner;
        this.publisherGuessingService = publisherGuessingService;
    }
    
    public Newsitem createNewsitemFromTwitterReply(String message, Newsitem newsitem, String submitter) {							
		if (isValidMessage(message)) {
    	
	    	newsitem.setTwitterSubmitter(submitter);
			newsitem.setTwitterMessage(message);
	        
			message = message.replaceFirst("@.*? ", "");
			String titleText = message.replaceFirst("http.*", "").trim();
			newsitem.setName(titleText);
	
			String url = message.replace(titleText, "").trim();	
	        if (url != "") {
	            newsitem.setUrl(urlCleaner.cleanSubmittedItemUrl(url));
	            newsitem.setDate(Calendar.getInstance().getTime());            
	            Website publisher = publisherGuessingService.guessPublisherBasedOnUrl(newsitem.getUrl());
	            newsitem.setPublisher(publisher);            
	            return newsitem;
	            
	        } else {
	            log.warn("Could not resolve url from twit");
	            return null;
	        }
		}
		log.info("Not a valid message: " + message);
		return null;
	}

	private boolean isValidMessage(String message) {
		return message.startsWith("@wellynews ");
	}
    
}
