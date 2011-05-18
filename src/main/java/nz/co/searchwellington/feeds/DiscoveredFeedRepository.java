package nz.co.searchwellington.feeds;

import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.commentfeeds.CommentFeedDetectorService;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.repositories.ResourceRepository;

public class DiscoveredFeedRepository {
	
	private ResourceRepository resourceDAO;
	private CommentFeedDetectorService commentFeedDetectorService;
	
	

	public DiscoveredFeedRepository(ResourceRepository resourceDAO, CommentFeedDetectorService commentFeedDetectorService) {
		this.resourceDAO = resourceDAO;
		this.commentFeedDetectorService = commentFeedDetectorService;
	}


	public List<DiscoveredFeed> getAllNonCommentDiscoveredFeeds() {	// TODO this is fantasically expensive - move to the index?
		List<DiscoveredFeed> allDiscoveredFeeds = resourceDAO.getAllDiscoveredFeeds();
        List<DiscoveredFeed> nonCommentFeeds = new ArrayList<DiscoveredFeed>();        
        for (DiscoveredFeed discoveredFeed : allDiscoveredFeeds) {
        	if (!commentFeedDetectorService.isCommentFeedUrl(discoveredFeed.getUrl())) {
        		nonCommentFeeds.add(discoveredFeed);
        	}
		}
		return nonCommentFeeds;
	}

}
