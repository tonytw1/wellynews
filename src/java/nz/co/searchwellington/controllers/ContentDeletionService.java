package nz.co.searchwellington.controllers;

import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Watchlist;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SupressionService;

import org.apache.log4j.Logger;

public class ContentDeletionService {
	
    private static Logger log = Logger.getLogger(ContentDeletionService.class);
    	
	private SupressionService supressionService;
	private RssfeedNewsitemService rssfeedNewsitemService;
	private ResourceRepository resourceDAO;
	

	


	public ContentDeletionService(SupressionService supressionService,
			RssfeedNewsitemService rssfeedNewsitemService,
			ResourceRepository resourceDAO) {		
		this.supressionService = supressionService;
		this.rssfeedNewsitemService = rssfeedNewsitemService;
		this.resourceDAO = resourceDAO;
	}


	public void performDelete(Resource editResource) {
		if (editResource.getType().equals("W")) {
			removePublisherFromPublishersContent(editResource);            	
		}
		
		if (editResource.getType().equals("F")) {
			removeFeedFromFeedNewsitems((Feed) editResource);			
		}
		
		if (editResource.getType().equals("N")) {
			Newsitem deletedNewsitem = (Newsitem) editResource;
			if (deletedNewsitem.getFeed() != null) {
				suppressDeletedNewsitem(deletedNewsitem);
			}
		}		
		resourceDAO.deleteResource(editResource);
	}


	private void suppressDeletedNewsitem(Newsitem deletedNewsitem) {
		log.info("Deleting a newsitem which was accepted from a feed; checking for required supression");
		if (rssfeedNewsitemService.getFeedNewsitemByUrl(deletedNewsitem.getFeed(), deletedNewsitem.getUrl()) != null) {
			log.info("Deleting a newsitem whose url still appears in a feed; suppressing the url: " + deletedNewsitem.getUrl());			
			supressionService.suppressUrl(deletedNewsitem.getUrl());
		}
	}

		
	private void removePublisherFromPublishersContent(Resource editResource) {
    	Website publisher = (Website) editResource;
    	for (Newsitem newsitem : publisher.getNewsitems()) {
    		newsitem.setPublisher(null);
    		resourceDAO.saveResource(newsitem);					
    	}
    	for (Feed feed : publisher.getFeeds()) {
    		feed.setPublisher(null);
    		resourceDAO.saveResource(feed);					
    	}
    	for (Watchlist watchlist : publisher.getWatchlist()) {
    		watchlist.setPublisher(null);
    		resourceDAO.saveResource(watchlist);					
    	}
    }
    
	
    private void removeFeedFromFeedNewsitems(Feed feed) {    	
    	for (Newsitem newsitem : resourceDAO.getNewsitemsForFeed(feed)) {
    		newsitem.setFeed(null);
    		resourceDAO.saveResource(newsitem);					
    	}    	
    }

}
