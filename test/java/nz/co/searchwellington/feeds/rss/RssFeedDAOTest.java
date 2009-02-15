package nz.co.searchwellington.feeds.rss;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import junit.framework.TestCase;

public class RssFeedDAOTest extends TestCase {

	RssFeedDAO rssFeedDAO;
    RssCache rssCache;
    
    @Override
    protected void setUp() throws Exception {    	
    	rssCache = mock(RssCache.class);
    	rssFeedDAO = new RssFeedDAO(rssCache);
    }
    
    public void testShouldLookInRssCacheForPrefetchedSyndFeedObject() throws Exception {                
        rssFeedDAO.getFeedByUrl("http://testdata/rss");
        verify(rssCache).getFeedByUrl("http://testdata/rss");
    }
    	  
}
