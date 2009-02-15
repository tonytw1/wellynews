package nz.co.searchwellington.commentfeeds.detectors;

public class WellingtonistaCommentFeedDetector implements CommentFeedDetector {
    
	public boolean isValid(String url) {
		return url != null && url.startsWith("http://wellingtonista.com/crss/node/");
	}
	
}
