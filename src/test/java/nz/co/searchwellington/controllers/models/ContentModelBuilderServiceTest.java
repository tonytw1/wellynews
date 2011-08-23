package nz.co.searchwellington.controllers.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.views.JsonViewFactory;
import nz.co.searchwellington.views.RssViewFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

public class ContentModelBuilderServiceTest {
	
	@Mock RssViewFactory rssViewFactory;
	@Mock JsonViewFactory jsonViewFactory;
	@Mock ContentRetrievalService contentRetrievalService;
	ModelBuilder[] modelBuilders;
	
	MockHttpServletRequest request;
	@Mock View rssView;
	@Mock View jsonView;
	@Mock List<Tag> featuredTags;
	@Mock List<Tag> topLevelTags;
	
	ModelAndView validModelAndView;
	@Mock ModelBuilder invalidModelBuilder;
	@Mock ModelBuilder validModelBuilder;
	
	ContentModelBuilderService contentModelBuilderService;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		request = new MockHttpServletRequest();
		validModelAndView = new ModelAndView();
		
		modelBuilders = new ModelBuilder[2];
		modelBuilders[0] = invalidModelBuilder;
		modelBuilders[1] = validModelBuilder;
		request.setPathInfo("/something");
		
		when(invalidModelBuilder.isValid(request)).thenReturn(false);
		when(validModelBuilder.isValid(request)).thenReturn(true);
		when(validModelBuilder.populateContentModel(request)).thenReturn(validModelAndView);
		
		contentModelBuilderService = new ContentModelBuilderService(rssViewFactory, jsonViewFactory, contentRetrievalService,  modelBuilders);
	}
	
	@Test
	public void shouldDelegateModelBuildingToTheFirstBuildWhoSaysTheyAreValid() throws Exception {		
		assertEquals(validModelAndView, contentModelBuilderService.populateContentModel(request));		
	}
	
	@Test
	public void shouldReturnNullIfNoModelBuilderWasFoundForRequest() throws Exception {
		modelBuilders[1] = invalidModelBuilder;
		assertNull(contentModelBuilderService.populateContentModel(request));
	}
	
	@Test
	public void rssSuffixedRequestsShouldBeGivenTheRssView() throws Exception {
		when(rssViewFactory.makeView()).thenReturn(rssView);
		request.setPathInfo("/something/rss");
		assertEquals(rssView, contentModelBuilderService.populateContentModel(request).getView());
	}
	
	@Test
	public void jsonSuffixedRequestsShouldBeGivenTheRssView() throws Exception {
		when(jsonViewFactory.makeView()).thenReturn(jsonView);
		request.setPathInfo("/something/json");
		assertEquals(jsonView, contentModelBuilderService.populateContentModel(request).getView());
	}
	
	@Test
	public void jsonCallbackShouldBeAddedToJsonModelIfValid() throws Exception {
		when(jsonViewFactory.makeView()).thenReturn(jsonView);
		request.setPathInfo("/something/json");
		request.setParameter("callback", "callbackName");
		ModelAndView mv = contentModelBuilderService.populateContentModel(request);
		assertEquals("callbackName", mv.getModel().get("callback"));
	}
		
	@Test
	public void featuredTagsShouldBeAddedToHtmlViews() throws Exception {
		when(contentRetrievalService.getFeaturedTags()).thenReturn(featuredTags);		
		ModelAndView mv = contentModelBuilderService.populateContentModel(request);		
		assertEquals(featuredTags, mv.getModel().get("featuredTags"));
	}
	
	@Test
	public void topLevelTagsShouldBeAddedToHtmlViews() throws Exception {
		when(contentRetrievalService.getTopLevelTags()).thenReturn(topLevelTags);		
		ModelAndView mv = contentModelBuilderService.populateContentModel(request);
		assertEquals(topLevelTags, mv.getModel().get("top_level_tags"));
	}
	
	@Test
	public void testShouldAcceptValidCallbackName() throws Exception {
		assertTrue(contentModelBuilderService.isValidCallbackName("_callBack123"));
	}
	
	@Test
	public void testShouldRejectInvalidCallbackName() throws Exception {
		assertFalse(contentModelBuilderService.isValidCallbackName("callback()"));
	}
	
}
