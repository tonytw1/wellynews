package nz.co.searchwellington.utils;

import com.google.common.base.Strings;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import uk.co.eelpieconsulting.common.html.HtmlCleaner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class UrlFilters {

    private static final Logger log = Logger.getLogger(UrlFilters.class);

    private static final String HTTP_PREFIX = "http://";
    private static final String PHPSESSID = "PHPSESSID";
    private static final Pattern UTM_PARAMETERS = Pattern.compile("^utm_.*$");

    private static final HtmlCleaner htmlCleaner = new HtmlCleaner();
    
    public static URI stripPhpSession(URI uri) throws URISyntaxException {
        return removeQueryParameterFrom(uri, PHPSESSID);
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
    
	public static URI stripUTMParams(URI uri) throws URISyntaxException {
        return removeQueryParametersFrom(uri, UTM_PARAMETERS);
	}

    private static URI removeQueryParameterFrom(URI uri, String parameterName) throws URISyntaxException {
        Pattern p = Pattern.compile("^" + parameterName + "$");
        return removeQueryParametersFrom(uri, p);
    }

    private static URI removeQueryParametersFrom(URI uri, Pattern p) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(uri);
        List<NameValuePair> filteredParams = new ArrayList<>();
        for (NameValuePair param: uriBuilder.getQueryParams()) {
            if (p.matcher(param.getName()).matches()) {
                continue;
            }
            filteredParams.add(param);
        }
        return uriBuilder.setParameters(filteredParams).build();
    }

}