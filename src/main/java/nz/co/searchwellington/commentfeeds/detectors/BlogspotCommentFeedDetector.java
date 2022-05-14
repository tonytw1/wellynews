package nz.co.searchwellington.commentfeeds.detectors;

import java.net.URL;

public class BlogspotCommentFeedDetector implements CommentFeedDetector {

    public boolean isValid(URL url) {
        return url != null && url.toExternalForm().contains("blogspot.com")
                && url.toExternalForm().contains("/comments/");
    }
    
}
