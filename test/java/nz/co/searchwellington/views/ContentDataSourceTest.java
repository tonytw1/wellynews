package nz.co.searchwellington.views;

import java.util.List;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ContentRetrievalService;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ContentDataSourceTest {
	
	@Mock ContentRetrievalService contentRetrievalService;
	@Mock List<Resource> latestNewsitems;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testShouldProvideLatestNewsitems() throws Exception {
		ContentDataSource dataSource = new ContentDataSource(contentRetrievalService);
		when(contentRetrievalService.getLatestNewsitems(5)).thenReturn(latestNewsitems);		
		List<Resource> result = dataSource.getLatestNewsitems();
		assertEquals(latestNewsitems, result);
	}

}
