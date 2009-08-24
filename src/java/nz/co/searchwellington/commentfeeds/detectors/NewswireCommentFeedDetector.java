package nz.co.searchwellington.commentfeeds.detectors;

public class NewswireCommentFeedDetector implements CommentFeedDetector {
	
	public boolean isValid(String url) {		
		return url.matches("^http://www.newswire.co.nz/\\d{4}/\\d{2}/.*?/feed/$");				
	}
	
}
