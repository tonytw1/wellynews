package nz.co.searchwellington.commentfeeds.detectors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateRegexCommentFeedDetector implements CommentFeedDetector {

	static Pattern yearMonthDateRegex = Pattern.compile(".*\\d{4}/\\d{2}/\\d{2}.*");
	
    public boolean isValid(String url) {
    	Matcher matcher = yearMonthDateRegex.matcher(url);
    	return url != null && matcher.matches();
    }
    
}
