package nz.co.searchwellington.utils;

import com.google.common.base.Strings;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import uk.co.eelpieconsulting.common.html.HtmlCleaner;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class UrlFilters {
    
    private static final String HTTP_PREFIX = "http://";
    
	private static HtmlCleaner htmlCleaner = new HtmlCleaner();
    
    public static String stripPhpSession(String url) {
        final String parameterName = "PHPSESSID";
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            List<NameValuePair> filteredParams = new ArrayList<>();
            for (NameValuePair param: uriBuilder.getQueryParams()) {
                if (param.getName().equals(parameterName)) {
                    continue;
                }
                filteredParams.add(param);
            }
            return uriBuilder.setParameters(filteredParams).build().toString();

        } catch (URISyntaxException e) {
            return url;
        }
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