package nz.co.searchwellington.utils;

import com.google.common.base.Strings;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import uk.co.eelpieconsulting.common.html.HtmlCleaner;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class UrlFilters {

    private static final String HTTP_PREFIX = "http://";

    private static final Pattern PHPSESSION_PARAMETER = Pattern.compile("^" + "PHPSESSID" + "$");

    private static final Pattern UTM_PARAMETERS = Pattern.compile("^utm_.*$");

    private static final HtmlCleaner htmlCleaner = new HtmlCleaner();

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

    public static URL stripPhpSession(URL url) throws URISyntaxException, MalformedURLException {
        return removeQueryParametersFrom(url, PHPSESSION_PARAMETER);
    }

	public static URL stripUTMParams(URL url) throws URISyntaxException, MalformedURLException {
        return removeQueryParametersFrom(url, UTM_PARAMETERS);
	}

    private static URL removeQueryParametersFrom(URL url, Pattern p) throws URISyntaxException, MalformedURLException {
        URIBuilder uriBuilder = new URIBuilder(url.toURI());
        List<NameValuePair> filteredParams = new ArrayList<>();
        for (NameValuePair param: uriBuilder.getQueryParams()) {
            if (p.matcher(param.getName()).matches()) {
                continue;
            }
            filteredParams.add(param);
        }
        return uriBuilder.setParameters(filteredParams).build().toURL();
    }

}