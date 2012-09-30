package nz.co.searchwellington.feeds;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import nz.co.searchwellington.dates.DateFormatter;
import nz.co.searchwellington.model.Comment;
import nz.co.searchwellington.model.CommentFeed;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.repositories.ConfigDAO;
import nz.co.searchwellington.repositories.HibernateResourceDAO;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.syndication.io.FeedException;

@Component
// TODO Should be a queue?
public class CommentFeedReader {
    
	private static Logger log = Logger.getLogger(CommentFeedReader.class);
	
    private static final int MAX_COMMENT_FEEDS_TO_LOAD = 30;
    
    private HibernateResourceDAO resourceDAO;   
    private CommentFeedService commentFeedService;
    private ContentUpdateService contentUpdateService;
    private ConfigDAO configDAO;
        
    public CommentFeedReader() {        
    }
    
    @Autowired
    public CommentFeedReader(HibernateResourceDAO resourceDAO,
			CommentFeedService commentFeedService,
			ContentUpdateService contentUpdateService, ConfigDAO configDAO) {
		this.resourceDAO = resourceDAO;
		this.commentFeedService = commentFeedService;
		this.contentUpdateService = contentUpdateService;
		this.configDAO = configDAO;
	}
    
	@Transactional
    public void loadComments() throws FeedException, IOException {
		boolean feedsAreEnabled = configDAO.isFeedReadingEnabled();
    	if (!feedsAreEnabled) {
    		log.info("Not fetching comments as feeds are disabled by config.");
    		return;
    	}
		
        log.info("Starting loading Comments.");
        List<CommentFeed> commentFeedsToRead = resourceDAO.getCommentFeedsToCheck(MAX_COMMENT_FEEDS_TO_LOAD);      
		log.info("Reading " + commentFeedsToRead.size() + " comment feeds.");               
        for (CommentFeed commentFeed : commentFeedsToRead) {                        
            loadCommentsFromCommentFeed(commentFeed);
            if (commentFeed.getNewsitem() != null) {
            	contentUpdateService.update(commentFeed.getNewsitem());
            }
        }
        log.info("Finished loading comments.");
	}
	
    public void loadCommentsFromCommentFeed(CommentFeed commentFeed) {    	
    	DateFormatter dateFormatter = new DateFormatter();    	
        if (hasntBeenReadInTheLastHour(commentFeed)) {
            log.info("Loading comments for comment feed: " + commentFeed.getUrl());
            loadCommentsForCommentFeed(commentFeed);
        } else {
            log.info("Skipping loading of comments for: " + commentFeed.getUrl() + " (last read " + dateFormatter.timeSince(commentFeed.getLastRead()) +")");
        }
    }
    
    private boolean hasntBeenReadInTheLastHour(CommentFeed commentFeed) {                    
        if (commentFeed != null && commentFeed.getLastRead() == null) {
            return true;
        }        
        Calendar loadCutoffTime = Calendar.getInstance();        
        loadCutoffTime.add(Calendar.HOUR, -(1));
    	if (commentFeed.getLastRead().after( loadCutoffTime.getTime())) {
    		return false;
    	}        
    	return true;        
	}
    
    private void loadCommentsForCommentFeed(CommentFeed commentFeed) {
    	log.info("Loading comments from url: " + commentFeed.getUrl());
    	final List<Comment> loadedComments = commentFeedService.loadComments(commentFeed);
    	log.info("Loaded " + loadedComments.size() + " comments from url: " + commentFeed.getUrl());
        commentFeed.getComments().clear();
        commentFeed.getComments().addAll(loadedComments);
        commentFeed.setLastRead(Calendar.getInstance().getTime());           
    }
    
}
