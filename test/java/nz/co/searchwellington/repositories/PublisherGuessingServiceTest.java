package nz.co.searchwellington.repositories;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.WebsiteImpl;

public class PublisherGuessingServiceTest extends TestCase {
	
		
	public void testShouldNotMatchIfNoMatchingPublishers() throws Exception {
		List<Resource> possiblePublishers = new ArrayList<Resource>();
		ResourceRepository resourceDAO = mock(ResourceRepository.class);
		stub(resourceDAO.getAllPublishersMatchingStem("www.spammer.com", true)).toReturn(possiblePublishers);
		
		PublisherGuessingService service = new PublisherGuessingService(resourceDAO);
		assertEquals(null, service.guessPublisherBasedOnUrl("http://www.spammer.com"));
	}
	
	
	public void testShouldMatchIfMultipleAvailable() throws Exception {
		List<Resource> possiblePublishers = new ArrayList<Resource>();
		
		Website golfCourseSite = new WebsiteImpl();
		golfCourseSite.setUrl("http://www.wellington.govt.nz/services/berhgolf/index.html");
		possiblePublishers.add(golfCourseSite);
				
		Website heritageInventory = new WebsiteImpl();
		heritageInventory.setUrl("http://www.wellington.govt.nz/services/heritage/inventory/index.html");
		possiblePublishers.add(heritageInventory);
		
		Website wccMainSite = new WebsiteImpl();
		wccMainSite.setUrl("http://www.wellington.govt.nz");
		possiblePublishers.add(wccMainSite);
	
		ResourceRepository resourceDAO = mock(ResourceRepository.class);
		stub(resourceDAO.getAllPublishersMatchingStem("www.wellington.govt.nz", true)).toReturn(possiblePublishers);
		
		PublisherGuessingService service = new PublisherGuessingService(resourceDAO);	
		assertEquals(wccMainSite, service.guessPublisherBasedOnUrl("http://www.wellington.govt.nz/news/display-item.php?id=3542"));
	}
	
	
	public void testShouldMatchIfOnlyOnePossiblePublisher() throws Exception {
		List<Resource> possiblePublishers = new ArrayList<Resource>();
		
		Website wellingtonista = new WebsiteImpl();
		wellingtonista.setUrl("http://www.wellingtonista.com");
		possiblePublishers.add(wellingtonista);
		
		ResourceRepository resourceDAO = mock(ResourceRepository.class);
		stub(resourceDAO.getAllPublishersMatchingStem("www.wellingtonista.com", true)).toReturn(possiblePublishers);
		
		PublisherGuessingService service = new PublisherGuessingService(resourceDAO);	
		assertEquals(wellingtonista, service.guessPublisherBasedOnUrl("http://www.wellingtonista.com/a-week-of-it"));
	}
	
	
	public void testShouldNotMatchJustBecauseTheHostNameMatches() throws Exception {
		List<Resource> possiblePublishers = new ArrayList<Resource>();
		
		Website hostedOne = new WebsiteImpl();
		hostedOne.setUrl("http://homepages.paradise.net.nz/~titahi/");
		possiblePublishers.add(hostedOne);
				
		Website hostedTwo = new WebsiteImpl();
		hostedTwo.setUrl("http://homepages.ihug.co.nz/~waicoll/");
		possiblePublishers.add(hostedTwo);
		
		ResourceRepository resourceDAO = mock(ResourceRepository.class);
		stub(resourceDAO.getAllPublishersMatchingStem("homepages.paradise.net.nz", true)).toReturn(possiblePublishers);
		
		PublisherGuessingService service = new PublisherGuessingService(resourceDAO);	
		assertEquals(null, service.guessPublisherBasedOnUrl("http://homepages.ihug.co.nz/~spammer/"));
	}
	
}
