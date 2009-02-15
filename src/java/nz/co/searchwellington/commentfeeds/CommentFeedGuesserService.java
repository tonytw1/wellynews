package nz.co.searchwellington.commentfeeds;

import nz.co.searchwellington.commentfeeds.guessers.CommentFeedGuesser;

public class CommentFeedGuesserService {
   
    private CommentFeedGuesser[] guessers;
    
    
	public CommentFeedGuesserService(CommentFeedGuesser... guessers) {
		this.guessers = guessers;
	}


	public String guessCommentFeedUrl(String url) {		
        for (CommentFeedGuesser guesser : guessers) {
            if (guesser.isValid(url)) {
                return(guesser.guessCommentFeedUrl(url));
            }
        }       
        return null;
    }
    
}
