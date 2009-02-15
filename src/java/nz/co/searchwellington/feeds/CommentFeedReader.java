package nz.co.searchwellington.feeds;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import nz.co.searchwellington.dates.DateFormatter;
import nz.co.searchwellington.model.Comment;
import nz.co.searchwellington.model.CommentFeed;
import nz.co.searchwellington.repositories.CommentDAO;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.sun.syndication.io.FeedException;


public class CommentFeedReader {
    
    private static final int MAX_COMMENT_FEEDS_TO_LOAD = 10;

    Logger log = Logger.getLogger(CommentFeedReader.class);
    
    private ResourceRepository resourceDAO;   
    private CommentDAO commentDAO;
    
    
    public CommentFeedReader() {        
    }
    
    
    public CommentFeedReader(ResourceRepository resourceDAO, CommentDAO commentDAO) {    
        this.resourceDAO = resourceDAO;        
        this.commentDAO = commentDAO;
    }

     
    @Transactional
    public void loadComments() throws FeedException, IOException {      
        log.info("Starting loading Comments.");      
        List<CommentFeed> commentFeedsToRead = resourceDAO.getCommentFeedsToCheck(MAX_COMMENT_FEEDS_TO_LOAD);      
		log.info("Reading " + commentFeedsToRead.size() + " comment feeds.");               
        for (CommentFeed commentFeed : commentFeedsToRead) {                        
            loadCommentsFromCommentFeed(commentFeed);
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
    	final List<Comment> loadedComments = commentDAO.loadComments(commentFeed);
        commentFeed.getComments().clear();
        commentFeed.getComments().addAll(loadedComments);
        commentFeed.setLastRead(Calendar.getInstance().getTime());        
        // TODO Must update the newsitem(s) to ensure that the index is updated.
    }
    
}
