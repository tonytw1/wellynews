package nz.co.searchwellington.filters;

import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GoogleSearchTermExtractor {

	Pattern pattern;
	
	public GoogleSearchTermExtractor() {
		pattern = Pattern.compile("http://www.google.*/search.*&q=(.*?)&.*");	
	}
	
	public String extractSearchTerm(String referrer) {
		Matcher matcher = pattern.matcher(referrer);
		if (matcher.matches()) {
			String escapedSearchTerm = matcher.group(1);
			return URLDecoder.decode(escapedSearchTerm);
		}
		return null;		
	}

}
