package nz.co.searchwellington.commentfeeds;

import nz.co.searchwellington.commentfeeds.detectors.CommentFeedDetector;

import org.apache.log4j.Logger;

public class CommentFeedDetectorService {

    Logger log = Logger.getLogger(CommentFeedDetectorService.class);
    

	private CommentFeedDetector[] detectors;
    
    
	public CommentFeedDetectorService(CommentFeedDetector... detectors) {
		this.detectors = detectors;
	}

	
	public boolean isCommentFeedUrl(String url) {
		log.debug("Checking is comment feed url: " + url);
        for (CommentFeedDetector detector : detectors) {
            if (detector.isValid(url)) {
                return true;
            }
        }    
        return false;        
    }
	

}
