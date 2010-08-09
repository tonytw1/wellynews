package nz.co.searchwellington.commentfeeds.detectors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EyeOfTheFishCommentFeedDetector implements CommentFeedDetector {

	static Pattern commentFeedUrlPattern = Pattern.compile("^http://eyeofthefish.org/.*/feed/$");
	
	public boolean isValid(String url) {
		Matcher matcher = commentFeedUrlPattern.matcher(url);
		return url != null && matcher.matches();
	}
	
}
