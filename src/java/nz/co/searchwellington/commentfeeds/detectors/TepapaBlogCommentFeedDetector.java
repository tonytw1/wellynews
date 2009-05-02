package nz.co.searchwellington.commentfeeds.detectors;

public class TepapaBlogCommentFeedDetector implements CommentFeedDetector {

	public boolean isValid(String url) {		
		return url.matches("^http://blog.tepapa.govt.nz/\\d{4}/\\d{2}/\\d{2}/.*?/feed/$");
				
	}
			
}
