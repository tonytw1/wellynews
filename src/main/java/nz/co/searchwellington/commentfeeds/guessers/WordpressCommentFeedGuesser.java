package nz.co.searchwellington.commentfeeds.guessers;

import org.springframework.stereotype.Component;

@Component
public class WordpressCommentFeedGuesser implements CommentFeedGuesser {

    public boolean isValid(String url) {        
        if (url != null) {        
            return url.matches(".*\\?p=\\d+$");
        }
        return false; 
    }
        
    public String guessCommentFeedUrl(String url) {
        if (isValid(url)) {
            return guessCommentFeedUrlFromWordpressNumberedUrl(url);
        }
        return null;
    }
     
    private String guessCommentFeedUrlFromWordpressNumberedUrl(String url) {
        if (url != null) {
            return url.replace("?p=", "?feed=rss2&p=");
        }
        return null;
    }
        
}
