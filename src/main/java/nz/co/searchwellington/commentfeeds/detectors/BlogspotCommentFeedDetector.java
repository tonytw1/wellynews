package nz.co.searchwellington.commentfeeds.detectors;

import org.springframework.stereotype.Component;

import java.net.URL;

@Component
public class BlogspotCommentFeedDetector implements CommentFeedDetector {

    public boolean isValid(URL url) {
        return url != null && url.toExternalForm().contains("blogspot.com")
                && url.toExternalForm().contains("/comments/");
    }
    
}
