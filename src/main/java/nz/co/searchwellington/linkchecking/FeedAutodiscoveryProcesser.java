package nz.co.searchwellington.linkchecking;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import nz.co.searchwellington.commentfeeds.CommentFeedDetectorService;
import nz.co.searchwellington.commentfeeds.CommentFeedGuesserService;
import nz.co.searchwellington.htmlparsing.CompositeLinkExtractor;
import nz.co.searchwellington.model.CommentFeed;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.HibernateResourceDAO;
import nz.co.searchwellington.repositories.ResourceFactory;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeedAutodiscoveryProcesser implements LinkCheckerProcessor {
	
    private static Logger log = Logger.getLogger(FeedAutodiscoveryProcesser.class);
	
    final private HibernateResourceDAO resourceDAO;	
	final private CompositeLinkExtractor linkExtractor;
	final private CommentFeedDetectorService commentFeedDetector;
	final private CommentFeedGuesserService commentFeedGuesser;
	final private ResourceFactory resourceFactory;
	
	@Autowired
	public FeedAutodiscoveryProcesser(HibernateResourceDAO resourceDAO,
			CompositeLinkExtractor linkExtractor,
			CommentFeedDetectorService commentFeedDetector,
			CommentFeedGuesserService commentFeedGuesser,
			ResourceFactory resourceFactory) {
		this.resourceDAO = resourceDAO;
		this.linkExtractor = linkExtractor;
		this.commentFeedDetector = commentFeedDetector;
		this.commentFeedGuesser = commentFeedGuesser;
		this.resourceFactory = resourceFactory;
	}
	
	public void process(Resource checkResource, String pageContent) {
		if (checkResource.getType().equals("F")) {
			return;
		}
		
		if (pageContent == null) {	// TODO Push up to calling service
			log.warn("Page content was null");
			return;
		}
		
		for (Iterator<String> iter = linkExtractor.extractLinks(pageContent).iterator(); iter.hasNext();) {
		    String discoveredUrl = (String) iter.next();
		    log.info("Processing discovered url: " + discoveredUrl);
		    if (!discoveredUrl.startsWith("http://")) {
		        log.info("url is not fully qualified: " + discoveredUrl);
				try {
					final String sitePrefix = new URL(checkResource.getUrl()).getHost();
					discoveredUrl = "http://" + sitePrefix + discoveredUrl;
		            log.info("url expanded to: " + discoveredUrl);
				} catch (MalformedURLException e) {
					log.error("Invalid url", e);
				}
		    }
		    
		    boolean isCommentFeedUrl = commentFeedDetector.isCommentFeedUrl(discoveredUrl);            
		    if (isCommentFeedUrl) {
		        log.debug("Discovered url is a comment feed: " + discoveredUrl);
		        if (checkResource.getType().equals("N")) {        
		            recordCommentFeed(checkResource, discoveredUrl);
		        }
		    } else {
		    	final boolean isUrlOfExistingFeed = resourceDAO.loadFeedByUrl(discoveredUrl) != null;
				if (!isUrlOfExistingFeed) {
		    		recordDiscoveredFeedUrl(checkResource, discoveredUrl);
		    	} else {
		    		log.debug("Not recording discovered feed as there is currently a feed of the same url: " + discoveredUrl);
		    	}
		    }
		}
		
		if (checkResource.getType().equals("N")) {
		    addGuessedCommentFeeds(checkResource);
		}		
	}
	
	// TODO merge this with the discoveredFeedUrl method.
    private void recordCommentFeed(Resource checkResource, String commentFeedUrl) {
    	log.info("Recording comment feed url for '" + checkResource.getName() + "': " + commentFeedUrl);
        // TODO can hibernate take care of this?
        CommentFeed commentFeed = resourceDAO.loadCommentFeedByUrl(commentFeedUrl);   
        if (commentFeed == null) {
            log.debug("Comment feed url was not found in the database. Creating new comment feed: " + commentFeedUrl);
            commentFeed = resourceFactory.createNewCommentFeed(commentFeedUrl);                         
            resourceDAO.saveCommentFeed(commentFeed);
        }
        ((Newsitem) checkResource).setCommentFeed(commentFeed);      
    }

    private void recordDiscoveredFeedUrl(Resource checkResource, String discoveredFeedUrl) {        
    	DiscoveredFeed discoveredFeed = resourceDAO.loadDiscoveredFeedByUrl(discoveredFeedUrl);
    	if (discoveredFeed == null) {
    		log.info("Recording newly discovered feed url: " + discoveredFeedUrl);
    		discoveredFeed = resourceFactory.createNewDiscoveredFeed(discoveredFeedUrl);                  
    	}
	    discoveredFeed.getReferences().add(checkResource);
	    resourceDAO.saveDiscoveredFeed(discoveredFeed);
    }
    
    private void addGuessedCommentFeeds(Resource checkResource) {
    	String commentFeedUrl = commentFeedGuesser.guessCommentFeedUrl(checkResource.getUrl());
    	if (commentFeedUrl != null) {
    		recordCommentFeed(checkResource, commentFeedUrl);         
    	}
    }
    
}
