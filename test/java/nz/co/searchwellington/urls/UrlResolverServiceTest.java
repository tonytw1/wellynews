package nz.co.searchwellington.urls;

import java.net.URLDecoder;

import junit.framework.TestCase;

public class UrlResolverServiceTest extends TestCase {

	public void testDecodesUrls() throws Exception {
		// TODO implement test in AbstactRedirectResolver.
		final String resolvedUrl = "http://www.mtvictoria.org.nz/?q=node%2F128";
		System.out.println(URLDecoder.decode(resolvedUrl));
	}
	
}
