package nz.co.searchwellington.twitter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.utils.UrlCleaner;

public class TwitterNewsitemBuilderServiceTests extends TestCase {

    TwitterNewsitemBuilderService newsitemBuilder;
    UrlCleaner urlCleaner;
    Newsitem newsitem;
    
    @Override
    protected void setUp() throws Exception {     
        urlCleaner = mock(UrlCleaner.class);
        stub(urlCleaner.cleanSubmittedItemUrl("http://www.vuw.ac.nz/news")).toReturn("http://www.vuw.ac.nz/news");    
        newsitemBuilder = new TwitterNewsitemBuilderService(urlCleaner);
        newsitem = new NewsitemImpl();
    }
    
    
	public void testShouldBuildNewsitemFromReply() throws Exception {                
		final String replyText = "@wellynewslog The quick brown fox    http://www.vuw.ac.nz/news  ";
		newsitemBuilder.createNewsitemFromTwitterReply(replyText, newsitem, "@someone");
               
        verify(urlCleaner).cleanSubmittedItemUrl("http://www.vuw.ac.nz/news");
		assertEquals("The quick brown fox", newsitem.getName());
		assertEquals("http://www.vuw.ac.nz/news", newsitem.getUrl());
		assertNotNull(newsitem.getDate());
        assertEquals("@someone", newsitem.getTwitterSubmitter());
	}
	
}
