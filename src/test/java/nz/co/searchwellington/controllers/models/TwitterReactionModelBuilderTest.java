package nz.co.searchwellington.controllers.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

public class TwitterReactionModelBuilderTest {

	@Mock ContentRetrievalService contentRetrievalService;
	
	private ModelBuilder modelBuilder;	
	private MockHttpServletRequest request;
	@Mock List<FrontendResource> twitteredNewitems;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		request = new MockHttpServletRequest();
		modelBuilder = new TwitterReactionModelBuilder(contentRetrievalService);
	}
		
	@Test
	public void usesTheCorrectView() throws Exception {
		ModelAndView mv = new ModelAndView();
		assertEquals("twitter", modelBuilder.getViewName(mv));
	}
	
	@Test
	public void mainContentIsTwitteredNewsitems() throws Exception {
		Mockito.when(contentRetrievalService.getRecentedTwitteredNewsitems()).thenReturn(twitteredNewitems);
		ModelAndView mv = modelBuilder.populateContentModel(request);
		assertEquals(twitteredNewitems, mv.getModel().get("main_content"));
	}
	
	@Test
	public void shouldSetHeading() throws Exception {
		ModelAndView mv = modelBuilder.populateContentModel(request);
		assertEquals("Following the Wellington newslog on Twitter", mv.getModel().get("heading"));
	}
	
	@Test
	public void isValidForTwitterPath() throws Exception {
		request.setPathInfo("/twitter");
		assertTrue(modelBuilder.isValid(request));
	}
	
}
