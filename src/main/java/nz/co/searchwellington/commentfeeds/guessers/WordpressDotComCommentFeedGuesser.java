package nz.co.searchwellington.commentfeeds.guessers;

public class WordpressDotComCommentFeedGuesser implements CommentFeedGuesser {
	
	public boolean isValid(String url) {
		return url != null && url.matches("http://.*\\.wordpress.com/.*?") && !url.matches("http://.*\\.wordpress.com/");		
	}
		
	public String guessCommentFeedUrl(String url) {
		if (isValid(url)) {
			return url + "feed/";
		}
		return null;
	}
	
}
