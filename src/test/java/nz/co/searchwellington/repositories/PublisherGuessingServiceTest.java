package nz.co.searchwellington.repositories;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.WebsiteImpl;

import com.google.common.collect.Lists;

public class PublisherGuessingServiceTest extends TestCase {
	
		
	public void testShouldNotMatchIfNoMatchingPublishers() throws Exception {
		List<Resource> possiblePublishers = Lists.newArrayList();
		HibernateResourceDAO resourceDAO = mock(HibernateResourceDAO.class);
		when(resourceDAO.getAllPublishersMatchingStem("www.spammer.com", true)).thenReturn(possiblePublishers);
		
		PublisherGuessingService service = new PublisherGuessingService(resourceDAO);
		assertEquals(null, service.guessPublisherBasedOnUrl("http://www.spammer.com"));
	}
	
	
	public void testShouldMatchIfMultipleAvailable() throws Exception {
		List<Resource> possiblePublishers = Lists.newArrayList();
		
		Website golfCourseSite = new WebsiteImpl();
		golfCourseSite.setUrl("http://www.wellington.govt.nz/services/berhgolf/index.html");
		possiblePublishers.add(golfCourseSite);
				
		Website heritageInventory = new WebsiteImpl();
		heritageInventory.setUrl("http://www.wellington.govt.nz/services/heritage/inventory/index.html");
		possiblePublishers.add(heritageInventory);
		
		Website wccMainSite = new WebsiteImpl();
		wccMainSite.setUrl("http://www.wellington.govt.nz");
		possiblePublishers.add(wccMainSite);
	
		HibernateResourceDAO resourceDAO = mock(HibernateResourceDAO.class);
		when(resourceDAO.getAllPublishersMatchingStem("www.wellington.govt.nz", true)).thenReturn(possiblePublishers);
		
		PublisherGuessingService service = new PublisherGuessingService(resourceDAO);	
		assertEquals(wccMainSite, service.guessPublisherBasedOnUrl("http://www.wellington.govt.nz/news/display-item.php?id=3542"));
	}
	
	
	public void testShouldMatchIfOnlyOnePossiblePublisher() throws Exception {
		List<Resource> possiblePublishers = Lists.newArrayList();
		
		Website wellingtonista = new WebsiteImpl();
		wellingtonista.setName("The Wellingtonista");
		wellingtonista.setUrl("http://www.wellingtonista.com");
		possiblePublishers.add(wellingtonista);
		
		HibernateResourceDAO resourceDAO = mock(HibernateResourceDAO.class);
		when(resourceDAO.getAllPublishersMatchingStem("www.wellingtonista.com", true)).thenReturn(possiblePublishers);
		
		PublisherGuessingService service = new PublisherGuessingService(resourceDAO);	
		assertEquals(wellingtonista, service.guessPublisherBasedOnUrl("http://www.wellingtonista.com/a-week-of-it"));
	}
	
	
	public void testShouldNotMatchJustBecauseTheHostNameMatches() throws Exception {
		List<Resource> possiblePublishers = Lists.newArrayList();
		
		Website hostedOne = new WebsiteImpl();
		hostedOne.setUrl("http://homepages.paradise.net.nz/~titahi/");
		possiblePublishers.add(hostedOne);
				
		Website hostedTwo = new WebsiteImpl();
		hostedTwo.setUrl("http://homepages.ihug.co.nz/~waicoll/");
		possiblePublishers.add(hostedTwo);
		
		HibernateResourceDAO resourceDAO = mock(HibernateResourceDAO.class);
		when(resourceDAO.getAllPublishersMatchingStem("homepages.paradise.net.nz", true)).thenReturn(possiblePublishers);
		
		PublisherGuessingService service = new PublisherGuessingService(resourceDAO);	
		assertEquals(null, service.guessPublisherBasedOnUrl("http://homepages.ihug.co.nz/~spammer/"));
	}
	
}
