package nz.co.searchwellington.commentfeeds.detectors;

import java.net.URL;

public class GenericCommentFeedDetector implements CommentFeedDetector {
	
	private String regex;
	
	public GenericCommentFeedDetector() {		
	}

	public GenericCommentFeedDetector(String regex) {
		this.regex = regex;
	}

	public boolean isValid(URL url) {
		return url != null && url.toExternalForm().matches(regex);
	}
	
	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

}
