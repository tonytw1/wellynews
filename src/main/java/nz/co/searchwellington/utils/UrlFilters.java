package nz.co.searchwellington.utils;

import com.google.common.base.Strings;
import org.apache.log4j.Logger;
import uk.co.eelpieconsulting.common.html.HtmlCleaner;

import java.util.regex.Pattern;

public class UrlFilters {
    
    private static final String HTTP_PREFIX = "http://";
    
	private static HtmlCleaner htmlCleaner = new HtmlCleaner();
    
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

    protected static String stripHttpPrefix(String url) {
        final String REGEX = "^http://";
        Pattern pattern = Pattern.compile(REGEX);
        return pattern.matcher(url).replaceFirst("");
    }
    
    public static String addHttpPrefixIfMissing(String url) {
        if (!Strings.isNullOrEmpty(url) && !hasHttpPrefix(url)) {
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
    
    public static String stripHtml(String content) {
		return htmlCleaner.stripHtml(content);        
    }
    
	public static String stripFeedburnerParams(String url) {
		Pattern p = Pattern.compile("[&|?]utm_.*(.*)$");
        return p.matcher(url).replaceAll("");
	}
	
}