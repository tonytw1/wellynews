package nz.co.searchwellington.commentfeeds.detectors;

public class BlogspotCommentFeedDetector implements CommentFeedDetector {

    public boolean isValid(String url) {
        return url != null && url.contains("blogspot.com") && url.contains("/comments/");
    }
    
}
