package nz.co.searchwellington.repositories;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Website;

import org.apache.log4j.Logger;

public class PublisherGuessingService {
    
    Logger log = Logger.getLogger(PublisherGuessingService.class);
    
    private ResourceRepository resourceDAO;
    
     
    public PublisherGuessingService(ResourceRepository resourceDAO) {    
        this.resourceDAO = resourceDAO;
    }


    public List<Resource> guessPossiblePublishersForUrl(String url) {
        String urlStem = calculateUrlStem(url);      
        final List<Resource> possiblePublishers = resourceDAO.getAllPublishersMatchingStem(urlStem, true);
        return possiblePublishers;
    }
    
    
    public Website guessPublisherBasedOnUrl(String url) {    
        List<Resource> possiblePublishers = guessPossiblePublishersForUrl(url);      
        if (possiblePublishers.size() == 1) {
            Website publisher = (Website) possiblePublishers.get(0);
            log.info("Guessing publisher for " + url + " is: " + publisher.getName());
            return publisher;           
        } else if (possiblePublishers.size() > 1) {
        	for (Resource possible : possiblePublishers) {        		
				if (url.startsWith(possible.getUrl())) {
					Website publisher = (Website) possible;
					return publisher;
				}
			}        	        	
        }
        return null;
    }
    
    
    private String calculateUrlStem(String fullURL) {
        String urlStem = null;        
        try {
            URL url = new URL(fullURL);
            String stem = new String(url.getHost());
            urlStem = stem;        
        } catch (MalformedURLException e) {
            urlStem = null;
        }        
        return urlStem;
    }
    

}
