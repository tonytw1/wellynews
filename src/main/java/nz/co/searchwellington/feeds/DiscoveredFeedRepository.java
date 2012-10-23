package nz.co.searchwellington.feeds;

import java.util.List;

import nz.co.searchwellington.commentfeeds.CommentFeedDetectorService;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.repositories.HibernateResourceDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class DiscoveredFeedRepository {
	
	private HibernateResourceDAO resourceDAO;
	private CommentFeedDetectorService commentFeedDetectorService;
	
	@Autowired
	public DiscoveredFeedRepository(HibernateResourceDAO resourceDAO, CommentFeedDetectorService commentFeedDetectorService) {
		this.resourceDAO = resourceDAO;
		this.commentFeedDetectorService = commentFeedDetectorService;
	}
	
	public List<DiscoveredFeed> getAllNonCommentDiscoveredFeeds() {	// TODO this is fantasically expensive - move to the index?
		List<DiscoveredFeed> nonCommentFeeds = Lists.newArrayList();       
        for (DiscoveredFeed discoveredFeed : resourceDAO.getAllDiscoveredFeeds()) {
        	// TODO Doing this at runtime is quite heavy
        	if (!commentFeedDetectorService.isCommentFeedUrl(discoveredFeed.getUrl())) {
        		nonCommentFeeds.add(discoveredFeed);
        	}
		}
		return nonCommentFeeds;
	}

}
