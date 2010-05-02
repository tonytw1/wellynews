package nz.co.searchwellington.twitter;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.PublisherGuessingService;
import nz.co.searchwellington.utils.UrlCleaner;

public class TwitterNewsitemBuilderServiceTests extends TestCase {

    TwitterNewsitemBuilderService newsitemBuilder;
    UrlCleaner urlCleaner;
    PublisherGuessingService publisherGuessingService;
    Newsitem newsitem;
    Website vuwPublisher;
    
    @Override
    protected void setUp() throws Exception {
   // 	vuwPublisher = mock(Website.class);
    //    urlCleaner = mock(UrlCleaner.class);
     //   publisherGuessingService = mock(PublisherGuessingService.class);
      //  stub(urlCleaner.cleanSubmittedItemUrl("http://www.vuw.ac.nz/news")).toReturn("http://www.vuw.ac.nz/news");
       // stub(publisherGuessingService.guessPublisherBasedOnUrl("http://www.vuw.ac.nz/news")).toReturn(vuwPublisher);
       // newsitemBuilder = new TwitterNewsitemBuilderService(urlCleaner, publisherGuessingService);
       // newsitem = new NewsitemImpl();
    }
    
    
	public void testShouldBuildNewsitemFromReply() throws Exception {                
//		final String replyText = "@wellynewslog The quick brown fox    http://www.vuw.ac.nz/news  ";
//		newsitemBuilder.createNewsitemFromTwitterReply(replyText, newsitem, "@someone");
               
    //    verify(urlCleaner).cleanSubmittedItemUrl("http://www.vuw.ac.nz/news");
	//	assertEquals("The quick brown fox", newsitem.getName());
	//	assertEquals("http://www.vuw.ac.nz/news", newsitem.getUrl());
	//	assertNotNull(newsitem.getDate());
     //   assertEquals("@someone", newsitem.getTwitterSubmitter());
      //  assertEquals(vuwPublisher, newsitem.getPublisher());
	}
	
	
	
	public void testCanFindUrlsInMessages() throws Exception {
		TwitterNewsitemBuilderService service = new TwitterNewsitemBuilderService(null, null, null, null);		
		assertEquals("http://tinyurl/test", service.extractUrlFromMessage("http://tinyurl/test"));
		assertEquals("http://tinyurl/test", service.extractUrlFromMessage("http://tinyurl/test "));
		assertEquals("http://tinyurl/test", service.extractUrlFromMessage(" http://tinyurl/test"));
	}
}
