package nz.co.searchwellington.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nz.co.searchwellington.urls.shorturls.CachingUrlResolverService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UrlCleanerTests {
	
	private static final String SECURE_URL = "https://www.secure/rss";
	private static final String URL_FROM_USERLAND = " www.capitalshakers.co.nz/news/genesis-energy-shakers-vs-otago-rebels-easter-sunday/?PHPSESSID=eb1ca7999e13201d6dbf02a02fffacdd  ";
	private static final String BASIC_CLEANED_URL = "http://www.capitalshakers.co.nz/news/genesis-energy-shakers-vs-otago-rebels-easter-sunday/?PHPSESSID=eb1ca7999e13201d6dbf02a02fffacdd";
	private static final String EXPECTED_CLEAN_URL = "http://www.capitalshakers.co.nz/news/genesis-energy-shakers-vs-otago-rebels-easter-sunday/";

	private static final String SHORT_URL = "http://short.url/12234";
	private static final String UNCLEANED_SHORT_URL = " http://short.url/12234  ";
	
	@Mock CachingUrlResolverService urlResolverService;
	
	UrlCleaner cleaner;
		
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);		
		when(urlResolverService.resolveUrl(BASIC_CLEANED_URL)).thenReturn(BASIC_CLEANED_URL);
		when(urlResolverService.resolveUrl(SHORT_URL)).thenReturn(BASIC_CLEANED_URL);
		when(urlResolverService.resolveUrl(SECURE_URL)).thenReturn(SECURE_URL);
		cleaner = new UrlCleaner(urlResolverService); 
	}
    
	@Test
    public void shouldStripPhpSessionId() throws Exception {
       assertEquals(EXPECTED_CLEAN_URL, cleaner.cleanSubmittedItemUrl(URL_FROM_USERLAND));
    }
    
	@Test
    public void shouldConsultRedirectingUrlResolvers() throws Exception {
    	String cleanedUrl = cleaner.cleanSubmittedItemUrl(SHORT_URL);
    	verify(urlResolverService).resolveUrl(SHORT_URL);
    	assertEquals(EXPECTED_CLEAN_URL, cleanedUrl);
    }
	
	@Test
    public void shouldCleanupShortUrlsBeforeResolving() throws Exception {
    	String cleanedUrl = cleaner.cleanSubmittedItemUrl(UNCLEANED_SHORT_URL);
    	verify(urlResolverService).resolveUrl(SHORT_URL);
    	assertEquals(EXPECTED_CLEAN_URL, cleanedUrl);
    }
	
	@Test
	public void testShouldPreappenedHttpPrefixIfMissing() throws Exception {
		String cleanedUrl = cleaner.cleanSubmittedItemUrl(UNCLEANED_SHORT_URL);
		assertEquals(EXPECTED_CLEAN_URL, cleanedUrl);
	}
	
	@Test
	public void testShouldAllowHttpsPrefix() throws Exception {
		String cleanedUrl = cleaner.cleanSubmittedItemUrl(SECURE_URL);
		assertEquals(SECURE_URL, cleanedUrl);

	}
	
}
