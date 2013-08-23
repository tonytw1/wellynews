package nz.co.searchwellington.feeds;

import junit.framework.TestCase;
import nz.co.searchwellington.urls.FeedBurnerRedirectResolver;
import nz.co.searchwellington.urls.RedirectingUrlResolver;

public class FeedBurnerRedirectResolverServiceTest extends TestCase {

	public void testShouldDetectFeedburnerProxyUrls() throws Exception {
		final String feedburnerProxyUrl = "http://feedproxy.google.com/~r/wellynews/~3/yGwOxeMzH68/09_04_29.htm";
		RedirectingUrlResolver resolver = new FeedBurnerRedirectResolver();
		assertTrue(resolver.isValid(feedburnerProxyUrl));
	}
}
