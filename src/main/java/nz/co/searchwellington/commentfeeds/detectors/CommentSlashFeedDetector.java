package nz.co.searchwellington.commentfeeds.detectors;

import org.springframework.stereotype.Component;

import java.net.URL;

@Component
public class CommentSlashFeedDetector implements CommentFeedDetector {

    @Override
    public boolean isValid(URL url) {
        return new GenericCommentFeedDetector("^.*\\/comments/feed\\/$").isValid(url);
    }
}
