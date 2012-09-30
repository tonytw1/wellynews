package nz.co.searchwellington.commentfeeds;

import nz.co.searchwellington.commentfeeds.detectors.CommentFeedDetector;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class CommentFeedDetectorService {

    private static Logger log = Logger.getLogger(CommentFeedDetectorService.class);
    
	private CommentFeedDetector[] detectors;
        
	@Autowired
	public CommentFeedDetectorService(@Qualifier("newswiresCommentFeedDetector") CommentFeedDetector... detectors) {	// TODO wire all
		this.detectors = detectors;
	}
	
	public boolean isCommentFeedUrl(String url) {
        for (CommentFeedDetector detector : detectors) {
        	log.debug(detector.getClass().getName() + " is checking is comment feed url: " + url);
            if (detector.isValid(url)) {
            	log.info(detector.getClass().getName() + " detected commnt feed url: " + url);
                return true;
            }
        }    
        return false;        
    }
	
}
