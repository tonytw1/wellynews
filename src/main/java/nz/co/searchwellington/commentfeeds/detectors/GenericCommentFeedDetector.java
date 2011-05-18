package nz.co.searchwellington.commentfeeds.detectors;

public class GenericCommentFeedDetector implements CommentFeedDetector {
	
	private String regex;
	
	public GenericCommentFeedDetector() {		
	}
	
	public boolean isValid(String url) {		
		return url != null && url.matches(regex);				
	}
	
	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

}
