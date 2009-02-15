package nz.co.searchwellington.commentfeeds;

import nz.co.searchwellington.commentfeeds.detectors.CommentFeedDetector;

public class CommentFeedDetectorService {

	
	private CommentFeedDetector[] detectors;
    
    
	public CommentFeedDetectorService(CommentFeedDetector... detectors) {
		this.detectors = detectors;
	}

	
	public boolean isCommentFeedUrl(String url) {
        for (CommentFeedDetector detector : detectors) {
            if (detector.isValid(url)) {
                return true;
            }
        }    
        return false;        
    }
	

}
