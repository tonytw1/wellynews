package nz.co.searchwellington.repositories;

import java.util.List;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.urls.UrlParser;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PublisherGuessingService {
    
    private static Logger log = Logger.getLogger(PublisherGuessingService.class);
    
    private HibernateResourceDAO resourceDAO;
	private UrlParser urlParser;
         
    @Autowired
    public PublisherGuessingService(HibernateResourceDAO resourceDAO,
    		UrlParser urlParser) {    
        this.resourceDAO = resourceDAO;
		this.urlParser = urlParser;
    }
    
    public List<Resource> guessPossiblePublishersForUrl(String url) {
        final String urlStem = urlParser.extractHostnameFrom(url);      
        return resourceDAO.getAllPublishersMatchingStem(urlStem, true);
    }
    
    public Website guessPublisherBasedOnUrl(String url) {    
        List<Resource> possiblePublishers = guessPossiblePublishersForUrl(url);      
        if (possiblePublishers.size() == 1) {
            Website publisher = (Website) possiblePublishers.get(0);
            log.debug("Guessing publisher for " + url + " is: " + publisher.getName());
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
    
}
