package nz.co.searchwellington.commentfeeds.guessers;

public interface CommentFeedGuesser {

    public boolean isValid(String url);
    
    public String guessCommentFeedUrl(String url);
        
}
