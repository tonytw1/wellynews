package nz.co.searchwellington.controllers;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.models.ContentModelBuilderService;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.ModelAndView;

public class TagControllerTest {

	@Mock ContentModelBuilderService contentModelBuilder;
	@Mock UrlStack urlStack;
	@Mock ContentRetrievalService contentRetrievalService;
	
	@Mock List<Tag> featuredTags;
	@Mock List<Tag> topLevelTags;
	
	private TagController tagController;
	private HttpServletRequest request;
	private HttpServletRequest unknownPathRequest;
	@Mock HttpServletResponse response;
	private ModelAndView modelAndHtmlView;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		modelAndHtmlView = new ModelAndView("a-view");
		Mockito.when(contentModelBuilder.populateContentModel(request)).thenReturn(modelAndHtmlView);
		tagController = new TagController(contentModelBuilder, urlStack, contentRetrievalService);
	}
	
	@Test
	public void shouldDelegateTotTheContentModelBuilderToGetTheModelForThisRequest() throws Exception {
		assertEquals(modelAndHtmlView, tagController.normal(request, response));
	}
	
	@Test
	public void should404IfNotModelWasAvailableForThisRequest() throws Exception {
		Mockito.when(contentModelBuilder.populateContentModel(unknownPathRequest)).thenReturn(null);

		tagController.normal(unknownPathRequest, response);
		
		Mockito.verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);		
	};
	
	@Test
	public void featuredTagsShouldBeAddedToHtmlViews() throws Exception {
		Mockito.when(contentRetrievalService.getFeaturedTags()).thenReturn(featuredTags);
		
		ModelAndView mv = tagController.normal(request, response);
		
		assertEquals(featuredTags, mv.getModel().get("featuredTags"));
	}
	
	@Test
	public void topLevelTagsShouldBeAddedToHtmlViews() throws Exception {
		Mockito.when(contentRetrievalService.getTopLevelTags()).thenReturn(topLevelTags);
		
		ModelAndView mv = tagController.normal(request, response);
		
		assertEquals(topLevelTags, mv.getModel().get("top_level_tags"));
	}
	
}
