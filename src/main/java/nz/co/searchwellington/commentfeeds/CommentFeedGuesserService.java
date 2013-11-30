package nz.co.searchwellington.commentfeeds;

import nz.co.searchwellington.commentfeeds.guessers.CommentFeedGuesser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommentFeedGuesserService {
   
    private CommentFeedGuesser[] guessers;
    
    @Autowired
	public CommentFeedGuesserService(CommentFeedGuesser... guessers) {
		this.guessers = guessers;
    }
    
	public String guessCommentFeedUrl(String url) {		
        for (CommentFeedGuesser guesser : guessers) {
            if (guesser.isValid(url)) {
                return guesser.guessCommentFeedUrl(url);
            }
        }       
        return null;
    }
    
}
