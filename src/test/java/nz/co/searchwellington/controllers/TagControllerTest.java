package nz.co.searchwellington.controllers;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.models.ContentModelBuilderService;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.repositories.TagDAO;

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
	
	private TagController tagController;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private ModelAndView modelAndHtmlView;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		modelAndHtmlView = new ModelAndView("a-view");
		tagController = new TagController(contentModelBuilder, urlStack, contentRetrievalService);
	}
	
	@Test
	public void featuredTagsShouldBeAddedToHtmlViews() throws Exception {
		Mockito.when(contentModelBuilder.populateContentModel(request)).thenReturn(modelAndHtmlView);
		Mockito.when(contentRetrievalService.getFeaturedTags()).thenReturn(featuredTags);
		
		ModelAndView mv = tagController.normal(request, response);
		
		assertEquals(featuredTags, mv.getModel().get("featuredTags"));
	}
	
}
