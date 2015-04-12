package nz.co.searchwellington.controllers.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import nz.co.searchwellington.controllers.models.helpers.CommonAttributesModelBuilder;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Lists;

public class SearchModelBuilderTest {
	
	@Mock ContentRetrievalService contentRetrievalService;
	@Mock UrlBuilder urlBuilder;
    @Mock CommonAttributesModelBuilder commonAttributesModelBuilder;
	
	@Mock Tag tag;
	private List<Tag> tags;
	private MockHttpServletRequest request;
	
	private SearchModelBuilder modelBuilder;
	@Mock List<FrontendResource> tagKeywordNewsitemResults;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		request = new MockHttpServletRequest();
		modelBuilder = new SearchModelBuilder(contentRetrievalService, urlBuilder, commonAttributesModelBuilder);
		tags = Lists.newArrayList();
		tags.add(tag);
	}
	
	@Test
	public void keywordShouldBeSetToIndicateASearch() throws Exception {
		request.setPathInfo("");
		assertFalse(modelBuilder.isValid(request));
		request.setParameter("keywords", "widgets");
		assertTrue(modelBuilder.isValid(request));
	}
	
	@Test
	public void pageHeadingShouldBeSearchKeyword() throws Exception {
		request.setParameter("keywords", "widgets");
		assertEquals("Search results - widgets", modelBuilder.populateContentModel(request).getModel().get("heading"));
	}
	
	@Test
	public void shouldGetTagRefinementResultsIfTagIsSet() throws Exception {
		request.setParameter("keywords", "widgets");
		request.setAttribute("tags", tags);
		Mockito.when(contentRetrievalService.getNewsitemsMatchingKeywords(Mockito.eq("widgets"), Mockito.eq(tag), Mockito.eq(0), Mockito.eq(30))).thenReturn(tagKeywordNewsitemResults);
		
		ModelAndView mv = modelBuilder.populateContentModel(request);
		
		assertEquals(tagKeywordNewsitemResults, mv.getModel().get("main_content"));
		assertEquals(tag, mv.getModel().get("tag"));
	}
	
}
