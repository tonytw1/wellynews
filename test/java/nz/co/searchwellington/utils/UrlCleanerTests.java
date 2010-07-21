package nz.co.searchwellington.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import junit.framework.TestCase;
import nz.co.searchwellington.urls.UrlResolverService;

import org.mockito.MockitoAnnotations;


public class UrlCleanerTests extends TestCase {
	
	final String capitalShakersUrl = "http://www.capitalshakers.co.nz/news/genesis-energy-shakers-vs-otago-rebels-easter-sunday/?PHPSESSID=eb1ca7999e13201d6dbf02a02fffacdd";

	UrlResolverService urlResolverService = mock(UrlResolverService.class);
	UrlCleaner cleaner;
		
	@Override
	protected void setUp() throws Exception {
		MockitoAnnotations.initMocks(UrlCleanerTests.class);
		when(urlResolverService.resolveUrl(capitalShakersUrl)).
		thenReturn(capitalShakersUrl);
		cleaner = new UrlCleaner(urlResolverService); 
	}
    
    public void testShouldStripPhpSessionId() throws Exception {
       assertEquals("http://www.capitalshakers.co.nz/news/genesis-energy-shakers-vs-otago-rebels-easter-sunday/", cleaner.cleanSubmittedItemUrl(capitalShakersUrl));        
    }
    
    public void testShouldConsultRedirectingUrlResolvers() throws Exception {
    	cleaner.cleanSubmittedItemUrl(capitalShakersUrl);
    	verify(urlResolverService).resolveUrl(capitalShakersUrl);
    }
    
}
