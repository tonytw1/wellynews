package nz.co.searchwellington.linkchecking;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.log4j.Logger;

import nz.co.searchwellington.commentfeeds.CommentFeedDetectorService;
import nz.co.searchwellington.commentfeeds.CommentFeedGuesserService;
import nz.co.searchwellington.htmlparsing.LinkExtractor;
import nz.co.searchwellington.model.CommentFeed;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ResourceRepository;

public class FeedAutodiscoveryProcesser implements LinkCheckerProcessor {
	
    private static Logger log = Logger.getLogger(FeedAutodiscoveryProcesser.class);

	
    private ResourceRepository resourceDAO;	
	private LinkExtractor linkExtractor;
	private CommentFeedDetectorService commentFeedDetector;
	private CommentFeedGuesserService commentFeedGuesser;
	
	
	


	public FeedAutodiscoveryProcesser(ResourceRepository resourceDAO,
			LinkExtractor linkExtractor,
			CommentFeedDetectorService commentFeedDetector,
			CommentFeedGuesserService commentFeedGuesser) {
		this.resourceDAO = resourceDAO;
		this.linkExtractor = linkExtractor;
		this.commentFeedDetector = commentFeedDetector;
		this.commentFeedGuesser = commentFeedGuesser;
	}


	public void process(Resource checkResource, String pageContent) {
		if (checkResource.getType().equals("F")) {
			return;
		}
		
		for (Iterator iter = linkExtractor.extractLinks(pageContent).iterator(); iter.hasNext();) {
		    String discoveredUrl = (String) iter.next();
		    
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
		        recordDiscoveredFeedUrl(checkResource, discoveredUrl);
		    }
		}
		
		if (checkResource.getType().equals("N")) {
		    addGuessedCommentFeeds(checkResource);
		}		
	}
	
	
	 // TODO merge this with the discoveredFeedUrl method.
    private void recordCommentFeed(Resource checkResource, String discoveredUrl) {
        // TODO can hibernate take care of this?
        CommentFeed commentFeed = resourceDAO.loadCommentFeedByUrl(discoveredUrl);                        
        if (commentFeed == null) {
            log.debug("Comment feed url was not found in the database. Creating new comment feed: " + discoveredUrl);
            commentFeed = resourceDAO.createNewCommentFeed(discoveredUrl);                         
            resourceDAO.saveCommentFeed(commentFeed);
        }
        ((Newsitem) checkResource).setCommentFeed(commentFeed);      
    }



	 private void recordDiscoveredFeedUrl(Resource checkResource, String discoveredUrl) {
	        DiscoveredFeed discoveredFeed = resourceDAO.loadDiscoveredFeedByUrl(discoveredUrl);
	        if (discoveredFeed == null) {
	            log.debug("Discovered feed url was not found in the database. Creating new: " + discoveredUrl);
	            discoveredFeed = resourceDAO.createNewDiscoveredFeed(discoveredUrl);                  
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
