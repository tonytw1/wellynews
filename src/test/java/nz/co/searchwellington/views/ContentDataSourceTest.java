package nz.co.searchwellington.views;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ContentDataSourceTest {
	
	@Mock ContentRetrievalService contentRetrievalService;
	@Mock List<FrontendResource> latestNewsitems;
	@Mock List<FrontendResource> publisherTagNewsitems;

	ContentDataSource dataSource;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		dataSource = new ContentDataSource(contentRetrievalService);
	}
	
	@Test
	public void shouldProvideLatestNewsitems() throws Exception {
		when(contentRetrievalService.getLatestNewsitems(5)).thenReturn(latestNewsitems);		
		List<FrontendResource> result = dataSource.getLatestNewsitems();
		assertEquals(latestNewsitems, result);
	}
	
	@Test
	public void shouldProvidePublisherTagCombinerNewsitems() throws Exception {
		when(contentRetrievalService.getPublisherTagCombinerNewsitems("publisher", "tag", 10)).thenReturn(publisherTagNewsitems);
		assertEquals(publisherTagNewsitems, dataSource.getPublisherTagNewsitems("publisher", "tag"));
	}

}
