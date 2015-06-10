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

public class ContentControllerTest {

	@Mock ContentModelBuilderService contentModelBuilder;
	@Mock UrlStack urlStack;

	private HttpServletRequest request;
	private HttpServletRequest unknownPathRequest;
	@Mock HttpServletResponse response;

    private ContentController contentController;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		contentController = new ContentController(contentModelBuilder, urlStack);
	}

	@Test
	public void shouldDelegateTotTheContentModelBuilderToGetTheModelForThisRequest() throws Exception {
		ModelAndView expectedModelAndView = new ModelAndView("a-view");
		Mockito.when(contentModelBuilder.populateContentModel(request)).thenReturn(expectedModelAndView);

		assertEquals(expectedModelAndView, contentController.normal(request, response));
	}
	
	@Test
	public void should404IfNotModelWasAvailableForThisRequest() throws Exception {
        Mockito.when(contentModelBuilder.populateContentModel(unknownPathRequest)).thenReturn(null);

		contentController.normal(unknownPathRequest, response);
		
		Mockito.verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

    @Test
    public void shouldNotPush404sOntoTheReturnToUrlStack() throws Exception {
        Mockito.when(contentModelBuilder.populateContentModel(unknownPathRequest)).thenReturn(null);

        contentController.normal(unknownPathRequest, response);

        Mockito.verifyZeroInteractions(urlStack);
    }

	@Test
	public void htmlPageViewsShouldBePutOntoTheUrlStack() throws Exception {
        ModelAndView expectedModelAndView = new ModelAndView("a-view");
        Mockito.when(contentModelBuilder.populateContentModel(request)).thenReturn(expectedModelAndView);

        contentController.normal(request, response);

		Mockito.verify(urlStack).setUrlStack(request);
	}
	
}
