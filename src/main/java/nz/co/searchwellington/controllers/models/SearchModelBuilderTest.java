package nz.co.searchwellington.controllers.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;
import nz.co.searchwellington.controllers.models.SearchModelBuilder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;

public class SearchModelBuilderTest {
	
	@Mock ContentRetrievalService contentRetrievalService;
	@Mock UrlBuilder urlBuilder;
	private MockHttpServletRequest request;
	
	private SearchModelBuilder modelBuilder;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		request = new MockHttpServletRequest();
		modelBuilder = new SearchModelBuilder(contentRetrievalService, urlBuilder);
	}
	
	@Test
	public void keywordShouldBeSetToIndicateASearch() throws Exception {
		assertFalse(modelBuilder.isValid(request));
		request.setParameter("keywords", "widgets");
		assertTrue(modelBuilder.isValid(request));
	}
	
	@Test
	public void pageHeadingShouldBeSearchKeyword() throws Exception {
		request.setParameter("keywords", "widgets");
		assertEquals("Search results - widgets", modelBuilder.populateContentModel(request, false).getModel().get("heading"));
	}
	
}
