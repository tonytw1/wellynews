package nz.co.searchwellington.controllers.ajax;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

public class PublisherAjaxControllerTest {

	@Mock ContentRetrievalService contentRetrievalService;

	@Mock List<String> publisherSuggestions;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	private PublisherAjaxController controller;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		controller = new PublisherAjaxController(contentRetrievalService);
		request.setParameter("q", "Wellington City");
	}
	
	@Test
	public void shouldReturnSuggestionsForPublishersWithNamesStartingWith() throws Exception {
		when(contentRetrievalService.getPublisherNamesByStartingLetters("Wellington City")).thenReturn(publisherSuggestions);
		
		ModelAndView mv = controller.handleRequest(request, response);
		
		assertEquals(publisherSuggestions, mv.getModel().get("suggestions"));
	}
	
	@Test
	public void shouldUseAjaxViewInReply() throws Exception {
		ModelAndView mv = controller.handleRequest(request, response);
		assertEquals("autocompleteData", mv.getViewName());
	}
	
}
