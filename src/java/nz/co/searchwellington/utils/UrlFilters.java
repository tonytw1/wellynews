package nz.co.searchwellington.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Provides methods for manipulating url strings.
 * 
 * @author tony
 *
 */
public class UrlFilters {
    
    private static final String UTF_8 = "UTF-8";
    private static final String HTTP_PREFIX = "http://";

    /**
     * Given a url string attempt to trim a trailing GET php session from the end.
     * @param url
     */
    protected static String stripPhpSession(String url) {      
        final String PHP_SESSION_REGEX = "[&\\?]PHPSESSID=[0-9|a-f]{32}";
        Pattern pattern = Pattern.compile(PHP_SESSION_REGEX);
        String result = pattern.matcher(url).replaceAll("");

        if (!result.equals(url)) {
            Logger.getLogger(UrlFilters.class).debug("stripPhpSession trimmed " + url + " to: " + result);
        }

        return result;
    }

    /**
     * Trim leading and trailing white space off a string.
     */
    public static String trimWhiteSpace(String title) {
        String result = title.trim();
        return result;
    }

    /**
     * Check is all letters in a sentence are capitals.
     * 
     * @param input
     * @return
     */
    protected static boolean isCapitalised(String input) {
        return (input.equals(StringUtils.upperCase(input)));
    }

    /**
     * TODO this is not a url filter! move.
     * Given a sentence, check if it is entirely captalised. 
     * If so lower case it, and captalise the first letter.
     * ie. 
     * THE QUICK BROWN FOX, becomes
     * The quick brown fox.
     * 
     * @param input
     * @return
     */
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

    /**
     * Given a URL string, strip the http:// prefix.
     */
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

    /**
     * Alot of userland url submissions lack the http:// prefix.
     * Return false if a submitted url string lacks this prefix.
     * @param url
     * @return
     */
    protected static boolean hasHttpPrefix(String url) {        
        final boolean urlIsLongerThanPrefix = url != null && url.length() >= HTTP_PREFIX.length();
        final boolean beginingOfUrlIsPrefix = urlIsLongerThanPrefix && url.substring(0, HTTP_PREFIX.length()).equals(HTTP_PREFIX);
        return urlIsLongerThanPrefix && beginingOfUrlIsPrefix;
    }

    /**
     * Add an http:// prefix to a string.
     * @param url
     * @return
     */
    protected static String addHttpPrefix(String url) {
        url = HTTP_PREFIX + url;
        return url;
    }

    

    public static String stripHtml(String content) {       
        Pattern p = Pattern.compile("<.*?>");
        return p.matcher(content).replaceAll("");    
    }

    public static String encode(String url) {
        try {
            return URLEncoder.encode(url, UTF_8);
        } catch (UnsupportedEncodingException e) {
            Logger.getLogger(UrlFilters.class).warn("Unsupported encoding while trying to encode url: " + url, e);            
            return url;
        }
    }

	public static String stripFeedburnerParams(String url) {
		Pattern p = Pattern.compile("&utm_.*(.*)$");
        return p.matcher(url).replaceAll("");
	}

    
}