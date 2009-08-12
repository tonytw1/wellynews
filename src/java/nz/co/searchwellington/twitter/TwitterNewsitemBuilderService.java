package nz.co.searchwellington.twitter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import net.unto.twitter.Status;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TwitteredNewsitem;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.PublisherGuessingService;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.utils.UrlCleaner;
import nz.co.searchwellington.views.ResourceDateConvertor;

import org.apache.log4j.Logger;

public class TwitterNewsitemBuilderService {

    Logger log = Logger.getLogger(TwitterNewsitemBuilderService.class);
    
    private UrlCleaner urlCleaner;
	private PublisherGuessingService publisherGuessingService;
	private ResourceRepository resourceDAO;
    
    public TwitterNewsitemBuilderService(UrlCleaner urlCleaner, PublisherGuessingService publisherGuessingService, ResourceRepository resourceDAO) {     
        this.urlCleaner = urlCleaner;
        this.publisherGuessingService = publisherGuessingService;
        this.resourceDAO = resourceDAO;
    }
    
    
    public List<TwitteredNewsitem> extractPossibleSubmissionsFromTwitterReplies(Status[] replies) {
    	List<TwitteredNewsitem> potentialTwitterSubmissions = new ArrayList<TwitteredNewsitem>();
    	for (Status status : replies) {   		
    		TwitteredNewsitem newsitem = this.createNewsitemFromTwitterReply(status
    				.getText(), status.getUser()
    				.getScreenName(), status.getId());
    		
    		if (newsitem != null && newsitem.getUrl() != null && !newsitem.getUrl().equals("")) {
    			boolean isRT = newsitem.getName() != null & newsitem.getName().startsWith("RT");
				if (!isRT) {					
					potentialTwitterSubmissions.add(newsitem);
				}
    		}
    	}
    	return potentialTwitterSubmissions;
    }
    
    
    
	public TwitteredNewsitem getTwitteredNewsitemByTwitterId(Long twitterId,
			List<TwitteredNewsitem> twitteredNewsitems) {
		TwitteredNewsitem newsitemToAccept = null;
		for (TwitteredNewsitem twitteredNewsitem : twitteredNewsitems) {
			log.info(twitteredNewsitem.getTwitterMessage() + ": " + twitteredNewsitem.getTwitterId());
			if (twitteredNewsitem.getTwitterId().longValue() == twitterId.longValue()) {
				newsitemToAccept = twitteredNewsitem;
			}
		}
		return newsitemToAccept;
	}
    
	
	
      
    private TwitteredNewsitem createNewsitemFromTwitterReply(String message, String submitter, long twitterId) {
		if (isValidMessage(message)) {			
			TwitteredNewsitem newsitem = resourceDAO.createNewTwitteredNewsitem(new Long(twitterId));
			
	    	newsitem.setTwitterSubmitter(submitter);
			newsitem.setTwitterMessage(message);			
			message = message.replaceFirst("@.*? ", "");
			String titleText = message.replaceFirst("http.*", "").trim();
			newsitem.setName(titleText);
	
			String url = message.replace(titleText, "").trim();
			// TODO trim trailing text after url.
			//  ie. @wellynews  RT Hutt City Council - Blind dates with books at your library http://tinyurl.com/n9j77c // Great minds meet! wcl_library too
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


	public Newsitem makeNewsitemFromTwitteredNewsitem(TwitteredNewsitem twitteredNewsitem) {
		// TODO constructor calls should be in the resourceDAO?
    	Newsitem newsitem = new NewsitemImpl(0, twitteredNewsitem.getName(), twitteredNewsitem.getUrl(), twitteredNewsitem.getDescription(), twitteredNewsitem.getDate(), null, 
    			new HashSet<Tag>(),
    			new HashSet<DiscoveredFeed>());   	
    	newsitem.setTwitterSubmitter(twitteredNewsitem.getTwitterSubmitter());
    	newsitem.setTwitterMessage(twitteredNewsitem.getTwitterMessage());
    	newsitem.setTwitterId(twitteredNewsitem.getTwitterId());
    	return newsitem;
	}
    
}
