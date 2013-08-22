package nz.co.searchwellington.urls;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class UrlParameterEncoder {

	private static final String UTF_8 = "UTF-8";
	
	public static String encode(String value) {
		try {
			return URLEncoder.encode(value, UTF_8);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
}
