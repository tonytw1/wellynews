package nz.co.searchwellington.controllers;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.models.ContentModelBuilderService;
import nz.co.searchwellington.model.Tag;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.ModelAndView;

public class TagControllerTest {

	@Mock ContentModelBuilderService contentModelBuilder;
	@Mock UrlStack urlStack;
	
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
		tagController = new TagController(contentModelBuilder, urlStack);
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
		Mockito.verifyZeroInteractions(urlStack);
	}
	
	@Test
	public void htmlPageViewsShouldBePutOntoTheUrlStack() throws Exception {
		tagController.normal(request, response);
		Mockito.verify(urlStack).setUrlStack(request);
	}
	
}
