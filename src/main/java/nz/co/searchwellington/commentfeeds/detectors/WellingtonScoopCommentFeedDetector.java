package nz.co.searchwellington.commentfeeds.detectors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WellingtonScoopCommentFeedDetector implements CommentFeedDetector {

	static Pattern commentFeedUrlPattern = Pattern.compile("^http://wellington.scoop.co.nz/\\?feed=rss2\\&p=(\\d+)$");
	static Pattern httpsCommentFeedUrlPattern = Pattern.compile("^https://wellington.scoop.co.nz/\\?feed=rss2\\&p=(\\d+)$");

	public boolean isValid(String url) {
		Matcher matcher = commentFeedUrlPattern.matcher(url);
		Matcher httpsMatcher = httpsCommentFeedUrlPattern.matcher(url);
		return url != null && (matcher.matches() || httpsMatcher.matches());
	}
	
}
