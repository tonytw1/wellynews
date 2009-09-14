package nz.co.searchwellington.twitter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.unto.twitter.Status;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TwitteredNewsitem;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.PublisherGuessingService;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.utils.UrlCleaner;

import org.apache.log4j.Logger;

public class TwitterNewsitemBuilderService {

    Logger log = Logger.getLogger(TwitterNewsitemBuilderService.class);
    
    private UrlCleaner urlCleaner;
	private PublisherGuessingService publisherGuessingService;
	private ResourceRepository resourceDAO;
	private TwitterService twitterService;
    
	
    public TwitterNewsitemBuilderService(UrlCleaner urlCleaner, PublisherGuessingService publisherGuessingService, 
    		ResourceRepository resourceDAO, TwitterService twitterService) {     
        this.urlCleaner = urlCleaner;
        this.publisherGuessingService = publisherGuessingService;
        this.resourceDAO = resourceDAO;
        this.twitterService = twitterService;
    }
    
    
	public List<TwitteredNewsitem> getPossibleSubmissions() {
		Status[] replies = twitterService.getReplies();
		return extractPossibleSubmissionsFromTwitterReplies(replies);
	}
    
	
	public void getRTs() {
		Status[] replies = twitterService.getReplies();
		for (Status status : replies) {
			final String message = status.getText();
			final String twitterName = '@' + twitterService.getUsername();
			if (message != null && message.contains(twitterName)) {				
				final String url = this.extractUrlFromMessage(message);
				if (url != null) {
					final String cleanedUrl = urlCleaner.cleanSubmittedItemUrl(url);
					log.info("Found url '" + cleanedUrl + "' in message: " + message);
					
					Resource resource = resourceDAO.loadResourceByUrl(cleanedUrl);
					if (resource != null) {
						log.info("Found RT: " + resource.getName() + ", " + message);
					}
				}
			}			
		}		
	}
	
	
	public TwitteredNewsitem getTwitteredNewsitemByTwitterId(Long twitterId, List<TwitteredNewsitem> twitteredNewsitems) {
		TwitteredNewsitem newsitemToAccept = null;
		for (TwitteredNewsitem twitteredNewsitem : twitteredNewsitems) {
			log.info(twitteredNewsitem.getTwitterMessage() + ": " + twitteredNewsitem.getTwitterId());
			if (twitteredNewsitem.getTwitterId().longValue() == twitterId.longValue()) {
				newsitemToAccept = twitteredNewsitem;
			}
		}
		return newsitemToAccept;
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

	    
    private List<TwitteredNewsitem> extractPossibleSubmissionsFromTwitterReplies(Status[] replies) {
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



	public String extractUrlFromMessage(String message) {
		Pattern pattern = Pattern.compile(".*(http://[\\S]+).*");
		Matcher matcher = pattern.matcher(message);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		return null;
	}

	
}
