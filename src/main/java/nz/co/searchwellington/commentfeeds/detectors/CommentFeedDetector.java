package nz.co.searchwellington.commentfeeds.detectors;

import java.net.URL;

public interface CommentFeedDetector {
    
    public boolean isValid(URL url);

}
