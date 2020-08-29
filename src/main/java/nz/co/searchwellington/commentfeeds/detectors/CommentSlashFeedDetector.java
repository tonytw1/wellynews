package nz.co.searchwellington.commentfeeds.detectors;

import org.springframework.stereotype.Component;

@Component
public class CommentSlashFeedDetector implements CommentFeedDetector {

    @Override
    public boolean isValid(String url) {
        System.out.println("dksjdkjs: " + url);
        return new GenericCommentFeedDetector("^.*\\/comments/feed\\/$").isValid(url);
    }
}
