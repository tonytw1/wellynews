package nz.co.searchwellington.utils;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class UrlFilters {
    
    private static final String HTTP_PREFIX = "http://";
    
    protected static String stripPhpSession(String url) {	// TODO wants to move to external feed lister service   
        final String PHP_SESSION_REGEX = "[&\\?]PHPSESSID=[0-9|a-f]{32}";
        Pattern pattern = Pattern.compile(PHP_SESSION_REGEX);
        String result = pattern.matcher(url).replaceAll("");

        if (!result.equals(url)) {
            Logger.getLogger(UrlFilters.class).debug("stripPhpSession trimmed " + url + " to: " + result);
        }

        return result;
    }
    
    public static String trimWhiteSpace(String title) {
        return title.trim();
    }
    
    // TODO this is not todo with urls; move.
    protected static boolean isCapitalised(String input) {
        return (input.equals(StringUtils.upperCase(input)));
    }
 
    // TODO this is not todo with urls; move.
    public static String lowerCappedSentence(String input) {
        String result = input;
        // Is the sentence entirely capitalised?
        if (isCapitalised(input)) {
            // Lower the string and then recapitalise.
            result = StringUtils.lowerCase(result);
            result = StringUtils.capitalize(result);
        }
        return result;
    }
    
    protected static String stripHttpPrefix(String url) {
        final String REGEX = "^http://";
        Pattern pattern = Pattern.compile(REGEX);
        return pattern.matcher(url).replaceFirst("");
    }
    
    public static String addHttpPrefixIfMissing(String url) {
        if (url != null && !url.equals("") && !hasHttpPrefix(url)) {
            Logger.getLogger(UrlFilters.class).info("Adding http:// prefix to submitted url: " + url);
            url = addHttpPrefix(url);
        }
        return url;
    }
    
    protected static boolean hasHttpPrefix(String url) {  
        return url.startsWith("http://") || url.startsWith("https://");
    }
    
    protected static String addHttpPrefix(String url) {
        url = HTTP_PREFIX + url;
        return url;
    }
    
    public static String stripHtml(String content) {	// TODO available in common-html    
        Pattern p = Pattern.compile("<.*?>");
        return p.matcher(content).replaceAll("");    
    }
    
	public static String stripFeedburnerParams(String url) {
		Pattern p = Pattern.compile("[&|?]utm_.*(.*)$");
        return p.matcher(url).replaceAll("");
	}
	
}