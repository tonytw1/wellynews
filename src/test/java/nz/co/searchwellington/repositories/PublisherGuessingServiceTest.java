package nz.co.searchwellington.repositories;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.WebsiteImpl;
import nz.co.searchwellington.urls.UrlParser;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class PublisherGuessingServiceTest extends TestCase {
		
	@Mock private UrlParser urlParser;
	@Mock private HibernateResourceDAO resourceDAO;
	
	private PublisherGuessingService service;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		service = new PublisherGuessingService(resourceDAO, urlParser);
	}

	public void testShouldNotMatchIfNoMatchingPublishers() throws Exception {
		List<Resource> possiblePublishers = Lists.newArrayList();
		HibernateResourceDAO resourceDAO = mock(HibernateResourceDAO.class);
		when(resourceDAO.getAllPublishersMatchingStem("www.spammer.com", true)).thenReturn(possiblePublishers);
		
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
	
		when(urlParser.extractHostnameFrom("http://www.wellington.govt.nz/news/display-item.php?id=3542")).thenReturn("www.wellington.govt.nz");
		when(resourceDAO.getAllPublishersMatchingStem("www.wellington.govt.nz", true)).thenReturn(possiblePublishers);
		
		assertEquals(wccMainSite, service.guessPublisherBasedOnUrl("http://www.wellington.govt.nz/news/display-item.php?id=3542"));
	}
	
	
	public void testShouldMatchIfOnlyOnePossiblePublisher() throws Exception {
		List<Resource> possiblePublishers = Lists.newArrayList();
		
		Website wellingtonista = new WebsiteImpl();
		wellingtonista.setName("The Wellingtonista");
		wellingtonista.setUrl("http://www.wellingtonista.com");
		possiblePublishers.add(wellingtonista);

		when(urlParser.extractHostnameFrom("http://www.wellingtonista.com/a-week-of-it")).thenReturn("www.wellingtonista.com");
		when(resourceDAO.getAllPublishersMatchingStem("www.wellingtonista.com", true)).thenReturn(possiblePublishers);
		
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
		
		when(resourceDAO.getAllPublishersMatchingStem("homepages.paradise.net.nz", true)).thenReturn(possiblePublishers);
		
		assertEquals(null, service.guessPublisherBasedOnUrl("http://homepages.ihug.co.nz/~spammer/"));
	}
	
}
